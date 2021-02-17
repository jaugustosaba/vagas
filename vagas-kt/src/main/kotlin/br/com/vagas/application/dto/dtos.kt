package br.com.vagas.application.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class JobDTO(
    @JsonProperty("empresa")
    val company: String,

    @JsonProperty("titulo")
    val title: String,

    @JsonProperty("descricao")
    val description: String,

    @JsonProperty("localizacao")
    val location: String,

    @JsonProperty("nivel")
    val level: Int
)

data class ApplicantDTO(
    @JsonProperty("nome")
    val name: String,

    @JsonProperty("profissao")
    val profession: String,

    @JsonProperty("localizacao")
    val location: String,

    @JsonProperty("nivel")
    val level: Int
)

data class ApplicationDTO(
    @JsonProperty("id_vaga")
    val job: Long,

    @JsonProperty("id_pessoa")
    val applicant: Long
)

data class RankDTO(
    @JsonProperty("nome")
    val name: String,

    @JsonProperty("profissao")
    val profession: String,

    @JsonProperty("localizacao")
    val location: String,

    @JsonProperty("nivel")
    val level: Int,

    @JsonProperty("score")
    val score: Int
)