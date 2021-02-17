package br.com.vagas.application.model

import br.com.vagas.application.dto.ApplicantDTO
import br.com.vagas.application.dto.ApplicationDTO
import br.com.vagas.application.dto.JobDTO
import br.com.vagas.application.dto.RankDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

open class VagasServiceException(message: String): RuntimeException(message)
class LocationNotFoundException(message: String): VagasServiceException(message)
class LevelNotFoundException(message: String): VagasServiceException(message)
class JobNotFoundException(message: String): VagasServiceException(message)
class ApplicantNotFoundException(message: String): VagasServiceException(message)

/**
 * Main API Service provider
 */
@Service
@Transactional
class VagasService {
    @Autowired
    lateinit var locationRepo: LocationRepository

    @Autowired
    lateinit var distanceRepo: DistanceRepository

    @Autowired
    lateinit var levelRepo: LevelRepository

    @Autowired
    lateinit var applicantRepo: ApplicantRepository

    @Autowired
    lateinit var jobRepo: JobRepository

    @Autowired
    lateinit var applicationRepo: ApplicationRepository


    private fun getLocationByName(name: String): Location =
        locationRepo.findByName(name) ?: throw LocationNotFoundException("unknown location: '$name'")

    private fun getLevelByValue(value: Int): Level =
        levelRepo.findById(value.toLong()).let { opt ->
            if (opt.isEmpty)
                throw LevelNotFoundException("unknown level: '$value'")
            opt.get()
        }


    /**
     * Creates a new applicant and returns his ID
     * @param dto
     *      The applicant data description
     * @return
     *      The ID of the newly created applicant
     * @throws LocationNotFoundException
     * @throws LevelNotFoundException
     */
    fun createApplicant(dto: ApplicantDTO): Long =
        applicantRepo.save(Applicant(
            id = null,
            name = dto.name,
            profession = dto.profession,
            location = getLocationByName(dto.location),
            level = getLevelByValue(dto.level),
            applications = listOf()
        )).id!!


    /**
     * Creates a new job and returns its ID
     * @param dto
     *      The job data description
     * @return
     *      The ID of the newly created job
     * @throws LocationNotFoundException
     * @throws LevelNotFoundException
     */
    fun createJob(dto: JobDTO): Long =
        jobRepo.save(Job(
            id = null,
            company = dto.company,
            title = dto.title,
            description = dto.description,
            location = getLocationByName(dto.location),
            level = getLevelByValue(dto.level),
            applications = listOf(),
        )).id!!


    /**
     * Registers an application to a job
     * @param dto
     *      The application data description (the applicant and the job)
     * @return
     *      true if the application is registered, false if it was already registered
     * @throws ApplicantNotFoundException
     * @throws JobNotFoundException
     */
    fun applyToJob(dto: ApplicationDTO): Boolean {
        val applicant = getApplicantById(dto.applicant)
        val job = getJobById(dto.job)
        applicationRepo.findById(ApplicationID(
                applicant = applicant,
                job = job
        )).let { opt ->
            if (opt.isPresent)
                return false
        }
        applicationRepo.save(Application(ApplicationID(
            applicant = applicant,
            job = job,
        )))
        return true
    }

    private fun getJobById(id: Long): Job =
        jobRepo.findById(id).let { opt ->
            if (opt.isEmpty)
                throw JobNotFoundException("unknown job for id=$id")
            return opt.get()
        }

    private fun getApplicantById(id: Long): Applicant =
        applicantRepo.findById(id).let { opt ->
            if (opt.isEmpty)
                throw ApplicantNotFoundException("unknown applicant for id=$id")
            return opt.get()
        }

    /**
     * Computes a distance between two locations
     * @param src
     *      The source location name
     * @param dest
     *      The destination location name
     * @return
     *      The distance between the two locations.
     *      Int.MAX_VALUE if does not have a path between src and dest.
     * @throws LocationNotFoundException
     */
    fun distance(src: String, dest: String): Int {
        val srcObj = getLocationByName(src)
        val destObj = getLocationByName(dest)
        return distance(srcObj, destObj)
    }

    private fun distance(src: Location, dest: Location): Int {
        val srcid = src.id!!
        val dists = mutableMapOf(srcid to 0)
        val frontier = mutableSetOf(src)
        val visited = mutableSetOf<Long>()
        fun dist(loc: Location) =
            dists.getOrDefault(loc.id, Int.MAX_VALUE)

        while (frontier.isNotEmpty()) {
            // takes a location on the frontier with the minimum distance
            val loc = frontier.toList().sortedBy{ dist(it) }.first()
            val d = dist(loc)

            // marks the location as visited
            frontier.remove(loc)
            visited.add(loc.id!!)

            // visits all neighbors in this location
            for (distance in distanceRepo.findAllNeighbors(loc)) {
                // treats distance as unidirectional
                val neighbor = distance.id!!.let { did ->
                    if (did.to_location.id == loc.id)
                        did.from_location
                    else
                        did.to_location
                }
                val nid = neighbor.id!!
                // if this node was visited before we already known the minimum distance
                if (visited.contains(nid))
                    continue
                // if reaching this neighbor has a distance less than the distance we currently known
                // we pass to use this path and update the distance to this location
                val step = distance.value
                val dd = d + step
                if (dd < dist(neighbor)) {
                    dists[nid] = dd
                    frontier.add(neighbor)
                }
            }
        }

        // Int.MAX_VALUE possible as a result if does not have a path to destination
        return dist(dest)
    }

    /**
     * Computes the rank for applicants for a job
     * @param jobId
     *      The job's ID
     * @return
     *      The applicants scored and sorted for a job
     * @throws JobNotFoundException
     */
    fun rank(jobId: Long): List<RankDTO> {
        val job = getJobById(jobId)
        return job.applications.map { application ->
            val applicant = application.id!!.applicant
            val N = 100 - 25 * abs(job.level.id!! - applicant.level.id!!).toInt()
            val dist = distance(applicant.location, job.location)
            val D = when (dist) {
                in 0..5    -> 100
                in 6..10   -> 75
                in 11..15  -> 50
                in 16..20  -> 25
                else       -> 0
            }
            val score = (N + D) / 2
            RankDTO(applicant.name, applicant.profession, applicant.location.name,
                applicant.level.id!!.toInt(), score)
        }.toList().sortedByDescending { rank ->
            rank.score
        }
    }
}