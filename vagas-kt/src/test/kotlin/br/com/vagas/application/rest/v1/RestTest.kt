package br.com.vagas.application.rest.v1

import br.com.vagas.application.dto.ApplicantDTO
import br.com.vagas.application.dto.ApplicationDTO
import br.com.vagas.application.dto.JobDTO
import br.com.vagas.application.dto.RankDTO
import br.com.vagas.application.model.VagasService
import com.fasterxml.jackson.annotation.JsonProperty
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

import org.springframework.test.web.servlet.RequestBuilder


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@ExtendWith(SpringExtension::class)
class RestTest {

    @Autowired
    lateinit var mvc: MockMvc

    @MockBean
    lateinit var service: VagasService

    @Test
    fun createJobTest() {
        val dto = JobDTO(
            company = "Teste",
            title = "Vaga teste",
            description = "Criar os mais diferentes tipos de teste",
            location = "A",
            level = 3,
        )
        Mockito.`when`(service.createJob(dto)).thenReturn(1234L)

        val rb: RequestBuilder = MockMvcRequestBuilders.post("/v1/vagas")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "empresa": "Teste",
                    "titulo": "Vaga teste",
                    "descricao": "Criar os mais diferentes tipos de teste",
                    "localizacao": "A",
                    "nivel": 3
                }
            """.trimIndent())

        val response = mvc.perform(rb).andReturn().response
        JSONAssert.assertEquals("""
            {
                "status": 200,
                "message": "OK",
                "result": 1234
            }
        """.trimIndent(), response.contentAsString, false)
    }

    @Test
    fun createApplicantTest() {
        val dto = ApplicantDTO(
            name = "John Doe",
            profession = "Engenheiro de Software",
            location = "C",
            level = 2
        )
        Mockito.`when`(service.createApplicant(dto)).thenReturn(43210L)

        val rb: RequestBuilder = MockMvcRequestBuilders.post("/v1/pessoas")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "nome": "John Doe",
                    "profissao": "Engenheiro de Software",
                    "localizacao": "C",
                    "nivel": 2
                }
            """.trimIndent())

        val response = mvc.perform(rb).andReturn().response
        JSONAssert.assertEquals("""
            {
                "status": 200,
                "message": "OK",
                "result": 43210
            }
        """.trimIndent(), response.contentAsString, false)
    }

    @Test
    fun createApplicationTest() {
        val dto = ApplicationDTO(
            job = 1234L,
            applicant = 43210L
        )
        Mockito.`when`(service.applyToJob(dto)).thenReturn(true)

        val rb: RequestBuilder = MockMvcRequestBuilders.post("/v1/candidaturas")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "id_vaga": 1234,
                    "id_pessoa": 43210
                }
            """.trimIndent())

        val response = mvc.perform(rb).andReturn().response
        JSONAssert.assertEquals("""
            {
                "status": 200,
                "message": "OK",
                "result": true
            }
        """.trimIndent(), response.contentAsString, false)
    }

    @Test
    fun rankingTest() {
        val rankings = arrayListOf(
            RankDTO(
                name = "Mary Jane",
                profession = "Engenheira de Software",
                location = "A",
                level = 4,
                score = 100,
            ),
            RankDTO(
                name = "John Doe",
                profession = "Engenheiro de Software",
                location = "C",
                level = 2,
                score = 85,
            ),
        )
        Mockito.`when`(service.rank(1234L)).thenReturn(rankings)

        val rb: RequestBuilder = MockMvcRequestBuilders.get("/v1/vagas/1234/candidaturas/ranking")
            .contentType(MediaType.APPLICATION_JSON)

        val response = mvc.perform(rb).andReturn().response
        JSONAssert.assertEquals("""
            [
                {
                    "nome": "Mary Jane",
                    "profissao": "Engenheira de Software",
                    "localizacao": "A",
                    "nivel": 4,
                    "score": 100
                },
                {
                    "nome": "John Doe",
                    "profissao": "Engenheiro de Software",
                    "localizacao": "C",
                    "nivel": 2,
                    "score": 85
                }
            ]
        """.trimIndent(), response.contentAsString, false)
    }
}