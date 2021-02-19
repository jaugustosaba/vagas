package main

import (
	"encoding/json"
	"fmt"
	"net/http"
	"os"
	"strconv"
	"vagas/model"
	"vagas/model/postgres"

	"github.com/gorilla/mux"
	_ "github.com/lib/pq"
)

type context struct {
	pg *postgres.Pg
}

func sendResult(response http.ResponseWriter, status int, result interface{}, errors []string) {
	payload := struct {
		Status  int         `json:"status"`
		Message string      `json:"message"`
		Result  interface{} `json:"result"`
		Error   []string    `json:"errors,omitempty"`
	}{
		Status:  status,
		Message: http.StatusText(status),
		Result:  result,
		Error:   errors,
	}
	response.WriteHeader(http.StatusBadRequest)
	encoder := json.NewEncoder(response)
	encoder.Encode(&payload)
}

func (ctx *context) vagas(response http.ResponseWriter, request *http.Request) {
	if request.Method != "POST" {
		sendResult(response, http.StatusBadRequest, nil, nil)
		return
	}
	decoder := json.NewDecoder(request.Body)
	var job struct {
		Company     string `json:"empresa"`
		Title       string `json:"titulo"`
		Description string `json:"descricao"`
		Location    string `json:"localizacao"`
		Level       int    `json:"nivel"`
	}
	if err := decoder.Decode(&job); err != nil {
		sendResult(response, http.StatusBadRequest, nil, []string{err.Error()})
		return
	}
	jobID, err := ctx.pg.AddJob(&model.Job{
		Company:     job.Company,
		Title:       job.Title,
		Description: job.Description,
		Location:    job.Location,
		Level:       job.Level,
	})
	if err != nil {
		sendResult(response, http.StatusInternalServerError, nil, []string{err.Error()})
		return
	}
	sendResult(response, http.StatusOK, jobID, nil)
}

func (ctx *context) candidaturas(response http.ResponseWriter, request *http.Request) {
	if request.Method != "POST" {
		sendResult(response, http.StatusBadRequest, nil, nil)
		return
	}
	decoder := json.NewDecoder(request.Body)
	var application struct {
		Job       model.JobID       `json:"id_vaga"`
		Applicant model.ApplicantID `json:"id_pessoa"`
	}
	if err := decoder.Decode(&application); err != nil {
		sendResult(response, http.StatusBadRequest, nil, []string{err.Error()})
		return
	}
	exists, err := ctx.pg.ApplyToJob(application.Job, application.Applicant)
	if err != nil {
		sendResult(response, http.StatusInternalServerError, nil, []string{err.Error()})
		return
	}
	sendResult(response, http.StatusOK, exists, nil)
}

func (ctx *context) pessoas(response http.ResponseWriter, request *http.Request) {
	if request.Method != "POST" {
		sendResult(response, http.StatusBadRequest, nil, nil)
		return
	}
	decoder := json.NewDecoder(request.Body)
	var applicant struct {
		Name       string `json:"nome"`
		Profession string `json:"profissao"`
		Location   string `json:"localizacao"`
		Level      int    `json:"nivel"`
	}
	if err := decoder.Decode(&applicant); err != nil {
		sendResult(response, http.StatusBadRequest, nil, []string{err.Error()})
		return
	}
	applicantID, err := ctx.pg.AddApplicant(&model.Applicant{
		Name:       applicant.Name,
		Profession: applicant.Profession,
		Location:   applicant.Location,
		Level:      applicant.Level,
	})
	if err != nil {
		sendResult(response, http.StatusInternalServerError, nil, []string{err.Error()})
		return
	}
	sendResult(response, http.StatusOK, applicantID, nil)
}

func (ctx *context) ranking(response http.ResponseWriter, request *http.Request) {
	if request.Method != "GET" {
		sendResult(response, http.StatusBadRequest, nil, nil)
		return
	}
	vars := mux.Vars(request)
	idStr := vars["id"]
	jobID, _ := strconv.ParseInt(idStr, 10, 64)
	ranking, err := model.Rank(ctx.pg, model.JobID(jobID))
	if err != nil {
		sendResult(response, http.StatusInternalServerError, nil, []string{err.Error()})
		return
	}
	sendResult(response, http.StatusOK, ranking, nil)
}

func main() {
	pg, err := postgres.New("user=vagas password=vagas dbname=vagas sslmode=disable")
	if err != nil {
		fmt.Printf("cannot initialize dabatase: %s", err.Error())
		os.Exit(1)
	}

	ctx := context{pg: pg}
	r := mux.NewRouter()
	s := r.PathPrefix("/v1").Subrouter()

	s.HandleFunc("/vagas", ctx.vagas)
	s.HandleFunc("/pessoas", ctx.pessoas)
	s.HandleFunc("/candidaturas", ctx.candidaturas)
	s.HandleFunc("/vagas/{id:[0-9]+}/candidaturas/ranking", ctx.ranking)

	http.ListenAndServe(":9000", r)
}
