package model

import (
	"math"
)

type ApplicantID int64
type JobID int64

type Job struct {
	Company     string
	Title       string
	Description string
	Location    string
	Level       int
}

type Applicant struct {
	Name       string
	Profession string
	Location   string
	Level      int
}

type Score struct {
	Applicant *Applicant
	Score     int
}

type Neighbor struct {
	Name     string
	Distance int
}

type Model interface {
	AddJob(job *Job) (JobID, error)
	AddApplicant(applicant *Applicant) (ApplicantID, error)
	ApplyToJob(job JobID, applicant ApplicantID) (bool, error)
	Neighbors(location string) ([]Neighbor, error)
	GetApplicants(job JobID) ([]*Applicant, error)
	GetJob(job JobID) (*Job, error)
}

func Distance(model Model, src, dest string) (int, error) {
	distances := map[string]int{}
	visited := map[string]bool{}
	frontier := map[string]bool{}

	distances[src] = 0
	frontier[src] = true
	distOf := func(loc string) int {
		d, exists := distances[loc]
		if !exists {
			return math.MaxInt32
		}
		return d
	}

	for len(frontier) > 0 {
		var loc string
		var d int
		for aLoc := range frontier {
			aDist := distOf(aLoc)
			if loc == "" || aDist < d {
				d = aDist
				loc = aLoc
			}
		}

		visited[loc] = true
		delete(frontier, loc)

		neighbors, err := model.Neighbors(loc)
		if err != nil {
			return 0, err
		}
		for _, neighbor := range neighbors {
			if visited[neighbor.Name] {
				continue
			}
			newDist := d + neighbor.Distance
			if newDist < distOf(neighbor.Name) {
				distances[neighbor.Name] = newDist
				frontier[neighbor.Name] = true
			}
		}
	}

	return distOf(dest), nil
}

func Rank(model Model, jobId JobID) (score []Score, err error) {
	var job *Job
	if job, err = model.GetJob(jobId); err != nil {
		return
	}
	var applicants []*Applicant
	if applicants, err = model.GetApplicants(jobId); err != nil {
		return
	}
	score = make([]Score, len(applicants))
	for i := range applicants {
		applicant := applicants[i]

		var distance int
		if distance, err = Distance(model, job.Location, applicant.Location); err != nil {
			return nil, err
		}

		N := 100 - 25*int(math.Abs(float64(job.Level-applicant.Level)))
		var D int
		if distance <= 5 {
			D = 100
		} else if distance <= 10 {
			D = 75
		} else if distance <= 15 {
			D = 50
		} else if distance <= 20 {
			D = 25
		} else {
			D = 0
		}

		score[i].Applicant = applicant
		score[i].Score = (N + D) / 2
	}

	return
}
