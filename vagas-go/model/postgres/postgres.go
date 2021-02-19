package postgres

import (
	"database/sql"
	"fmt"
	"vagas/model"
)

var ErrLocationNotFound = fmt.Errorf("Location not found")
var ErrLevelNotFound = fmt.Errorf("Level not found")
var ErrInvalidId = fmt.Errorf("Invalid ID")
var ErrInvalidLevel = fmt.Errorf("Invalid level ID")

type Pg struct {
	db *sql.DB
}

func New(dataSource string) (*Pg, error) {
	db, err := sql.Open("postgres", dataSource)
	if err != nil {
		return nil, nil
	}
	return &Pg{db: db}, nil
}

func (pg *Pg) getLocationByName(name string) (int64, error) {
	rows, err := pg.db.Query("SELECT id FROM location WHERE name = $1", name)
	if err != nil {
		return 0, nil
	}
	if !rows.Next() {
		return 0, ErrLocationNotFound
	}
	defer rows.Close()
	var id int64
	err = rows.Scan(&id)
	if err != nil {
		return 0, err
	}
	return id, nil
}

func (pg *Pg) getLevelByValue(value int64) (int64, error) {
	if value <= 0 {
		return 0, ErrInvalidLevel
	}
	rows, err := pg.db.Query("SELECT id FROM level WHERE id = $1", value)
	if err != nil {
		return 0, nil
	}
	if !rows.Next() {
		return 0, ErrLevelNotFound
	}
	defer rows.Close()
	return value, nil
}

func (pg *Pg) AddJob(job *model.Job) (model.JobID, error) {
	var err error
	var locID int64
	if locID, err = pg.getLocationByName(job.Location); err != nil {
		return 0, err
	}
	var levelID int64
	if levelID, err = pg.getLevelByValue(int64(job.Level)); err != nil {
		return 0, err
	}
	row := pg.db.QueryRow(
		"INSERT INTO job (company, title, description, location_id, level_id) VALUES ($1, $2, $3, $4, $5) RETURNING id",
		job.Company,
		job.Title,
		job.Description,
		locID,
		levelID,
	)
	var jobID int64
	if err = row.Scan(&jobID); err != nil {
		return 0, err
	}
	return model.JobID(jobID), nil
}

func (pg *Pg) AddApplicant(applicant *model.Applicant) (model.ApplicantID, error) {
	var err error
	var locID int64
	if locID, err = pg.getLocationByName(applicant.Location); err != nil {
		return 0, err
	}
	var levelID int64
	if levelID, err = pg.getLevelByValue(int64(applicant.Level)); err != nil {
		return 0, err
	}
	row := pg.db.QueryRow(
		"INSERT INTO applicant (name, profession, location_id, level_id) VALUES ($1, $2, $3, $4) RETURNING id",
		applicant.Name,
		applicant.Profession,
		locID,
		levelID,
	)
	var applicantID int64
	if row.Scan(&applicantID) != nil {
		return 0, nil
	}
	return model.ApplicantID(applicantID), nil
}

func (pg *Pg) findApplication(job model.JobID, applicant model.ApplicantID) (bool, error) {
	var err error
	var rows *sql.Rows
	rows, err = pg.db.Query(
		"SELECT 1 FROM application WHERE job_id = $1 AND applicant_id = $2",
		job,
		applicant,
	)
	if err != nil {
		return false, err
	}
	defer rows.Close()
	return rows.Next(), nil
}

func (pg *Pg) ApplyToJob(job model.JobID, applicant model.ApplicantID) (bool, error) {
	if job <= 0 {
		return false, ErrInvalidId
	}
	if applicant <= 0 {
		return false, ErrInvalidId
	}
	var err error
	var exists bool
	if exists, err = pg.findApplication(job, applicant); err != nil {
		return false, err
	}
	if exists {
		return false, nil
	}
	var result sql.Result
	result, err = pg.db.Exec("INSERT INTO application (applicant_id, job_id) VALUES ($1, $2)", applicant, job)
	if err != nil {
		return false, err
	}
	var affected int64
	affected, err = result.RowsAffected()
	if err != nil {
		return false, err
	}
	return affected > 0, nil
}

func (pg *Pg) Neighbors(location string) ([]model.Neighbor, error) {
	locID, err := pg.getLocationByName(location)
	if err != nil {
		return nil, nil
	}
	var rows *sql.Rows
	rows, err = pg.db.Query(
		`SELECT 
			L1.name, L2.name, value 
		FROM
			distance 
		JOIN
			location L1 ON L1.id = from_location_id
		JOIN
			location L2 ON L2.id = to_location_id
		WHERE
			from_location_id = $1 OR to_location_id = $1
		`,
		locID,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	neighbors := []model.Neighbor{}
	for rows.Next() {
		var from, to string
		var dist int
		if err = rows.Scan(&from, &to, &dist); err != nil {
			return nil, err
		}
		if to == location {
			to = from
		}
		neighbors = append(neighbors, model.Neighbor{
			Name:     to,
			Distance: dist,
		})
	}
	return neighbors, nil
}

func (pg *Pg) GetApplicants(job model.JobID) ([]*model.Applicant, error) {
	rows, err := pg.db.Query(
		`
		SELECT
			a.name, a.profession, l.name, a.level_id
		FROM
			application ap
		INNER JOIN
			applicant a ON ap.applicant_id = a.id
		INNER JOIN
			location l ON l.id = a.location_id
		WHERE
			ap.job_id = $1
		`,
		job,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	applicants := []*model.Applicant{}
	for rows.Next() {
		var name, profession, location string
		var level int
		if err := rows.Scan(&name, &profession, &location, &level); err != nil {
			return nil, err
		}
		applicants = append(applicants, &model.Applicant{
			Name:       name,
			Profession: profession,
			Location:   location,
			Level:      level,
		})
	}

	return applicants, nil
}

func (pg *Pg) GetJob(job model.JobID) (*model.Job, error) {
	row := pg.db.QueryRow(
		`
		SELECT
			j.company, j.title, j.description, l.name, j.level_id
		FROM
			job j
		INNER JOIN
			location l ON l.id = location_id
		WHERE
			j.id = $1
		`,
		job,
	)
	var company, title, description, location string
	var level int
	if err := row.Scan(&company, &title, &description, &location, &level); err != nil {
		return nil, err
	}
	return &model.Job{
		Company:     company,
		Title:       title,
		Description: description,
		Location:    location,
		Level:       level,
	}, nil
}
