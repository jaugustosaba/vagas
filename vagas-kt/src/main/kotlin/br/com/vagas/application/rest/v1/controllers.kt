package br.com.vagas.application.rest.v1

import br.com.vagas.application.dto.ApplicantDTO
import br.com.vagas.application.dto.ApplicationDTO
import br.com.vagas.application.dto.JobDTO
import br.com.vagas.application.dto.RankDTO
import br.com.vagas.application.model.*
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Response<T>(
    val status: Int,
    val message: String,
    val result: T?,
    val errors: List<String>,
) {
    companion object {
        fun <T> fromStatus(status: HttpStatus, result: T?, errors: List<String>) =
            Response(status.value(), status.reasonPhrase, result, errors)

        fun <T> ok(result: T): Response<T> =
            fromStatus(HttpStatus.OK, result, listOf())

    }
}

@RestController
@RequestMapping("/v1")
class Controller(@Autowired val service: VagasService) {

    @PostMapping("/vagas")
    fun createJob(@RequestBody job: JobDTO): Response<Long> =
        Response.ok(service.createJob(job))

    @PostMapping("/pessoas")
    fun createApplicant(@RequestBody applicant: ApplicantDTO): Response<Long> =
        Response.ok(service.createApplicant(applicant))

    @PostMapping("/candidaturas")
    fun applyJob(@RequestBody application: ApplicationDTO): Response<Boolean> =
        Response.ok(service.applyToJob(application))

    @GetMapping("/vagas/{id:[0-9]+}/candidaturas/ranking")
    fun computeRanking(@PathVariable("id") jobId: Long): List<RankDTO> =
        service.rank(jobId)

}

@RestControllerAdvice
class Handler: ResponseEntityExceptionHandler() {
    @ExceptionHandler(Throwable::class)
    fun genericError(ex: Throwable): Response<Any> =
        Response.fromStatus(HttpStatus.INTERNAL_SERVER_ERROR, null, listOf(ex.message!!))

    @ExceptionHandler(LocationNotFoundException::class)
    fun locationNotFound(ex: LocationNotFoundException): Response<Any> =
        Response.fromStatus(HttpStatus.NOT_FOUND, null, listOf(ex.message!!))

    @ExceptionHandler(LevelNotFoundException::class)
    fun levelNotFound(ex: LevelNotFoundException): Response<Any> =
        Response.fromStatus(HttpStatus.NOT_FOUND, null, listOf(ex.message!!))

    @ExceptionHandler(JobNotFoundException::class)
    fun jobNotFound(ex: JobNotFoundException): Response<Any> =
        Response.fromStatus(HttpStatus.NOT_FOUND, null, listOf(ex.message!!))

    @ExceptionHandler(ApplicantNotFoundException::class)
    fun applicantNotFound(ex: ApplicantNotFoundException): Response<Any> =
        Response.fromStatus(HttpStatus.NOT_FOUND, null, listOf(ex.message!!))
}