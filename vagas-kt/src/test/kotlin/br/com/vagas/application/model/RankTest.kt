package br.com.vagas.application.model

import br.com.vagas.application.dto.RankDTO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.math.abs


@SpringBootTest
@DirtiesContext
@AutoConfigureTestDatabase
@ExtendWith(SpringExtension::class)
class RankTest {

    @Autowired
    lateinit var service: VagasService

    @Autowired
    lateinit var jobRepo: JobRepository

    @Autowired
    lateinit var applicantRepo: ApplicantRepository

    @Autowired
    lateinit var locationRepo: LocationRepository

    @Autowired
    lateinit var distanceRepo: DistanceRepository

    @Autowired
    lateinit var levelRepo: LevelRepository

    @Autowired
    lateinit var applicationRepo: ApplicationRepository

    @Test
    fun rankTest() {
        // map
        val A = locationRepo.save(Location(null, "A"))
        val B = locationRepo.save(Location(null, "B"))
        distanceRepo.save(Distance(DistanceId(A, B), 15))

        // levels
        val entry = levelRepo.save(Level(null, "Entry"))
        val specialist = levelRepo.save(Level(null, "Specialist"))

        // applicants
        val mary = applicantRepo.save(Applicant(
            id = null,
            name = "Mary Jane",
            profession = "Worker",
            location = B,
            level = specialist,
            applications = arrayListOf()
        ))
        val john = applicantRepo.save(Applicant(
            id = null,
            name = "John Doe",
            profession = "Worker",
            location = A,
            level = entry,
            applications = arrayListOf()
        ))

        // job
        val job = jobRepo.save(Job(
            id = null,
            company = "Company",
            title = "Amazing Company",
            description = "Amazing Company needs workers",
            location = A,
            level = specialist,
            applications = arrayListOf()
        ))

        // applications
        applicationRepo.save(Application(ApplicationID(mary, job)))
        applicationRepo.save(Application(ApplicationID(john, job)))

        // verify results
        val ranking = service.rank(job.id!!)

        Assertions.assertEquals(listOf(
            RankDTO(
                name = john.name,
                profession = john.profession,
                location = john.location.name,
                level = john.level.id!!.toInt(),
                score = (100 + (100 - 25*abs(job.level.id!! - john.level.id!!).toInt())) / 2
            ),
            RankDTO(
                name = mary.name,
                profession = mary.profession,
                location = mary.location.name,
                level = mary.level.id!!.toInt(),
                score = (50 + (100 - 25* abs(job.level.id!! - mary.level.id!!).toInt())) / 2
            ),
        ), ranking)
    }
}