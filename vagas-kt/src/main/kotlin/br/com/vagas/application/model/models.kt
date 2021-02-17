package br.com.vagas.application.model

import java.io.Serializable
import javax.persistence.*

@Entity
class Location(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @Column(unique = true)
    var name: String
)

@Embeddable
data class DistanceId(
    @ManyToOne
    @JoinColumn(name="from_location_id")
    var from_location: Location,

    @ManyToOne
    @JoinColumn(name="to_location_id")
    var to_location: Location
): Serializable

@Entity
class Distance(
    @EmbeddedId
    var id: DistanceId?,

    var value: Int
): Serializable

@Entity
class Level(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,

    @Column(unique = true)
    var name: String
)

@Entity
class Applicant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,
    var name: String,
    var profession: String,
    @ManyToOne
    @JoinColumn(name="location_id")
    var location: Location,
    @ManyToOne
    @JoinColumn(name="level_id")
    var level: Level,
    @OneToMany(targetEntity = Application::class)
    var applications: List<Application>
)

@Entity
class Job(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,
    var company: String,
    var title: String,
    var description: String,
    @ManyToOne
    @JoinColumn(name="location_id")
    var location: Location,
    @ManyToOne
    @JoinColumn(name = "level_id")
    var level: Level,
    @OneToMany(targetEntity = Application::class, mappedBy = "id.job")
    var applications: List<Application>
)

@Embeddable
data class ApplicationID(
    @ManyToOne
    @JoinColumn(name="applicant_id")
    var applicant: Applicant,

    @ManyToOne
    @JoinColumn(name="job_id")
    var job: Job
): Serializable

@Entity
class Application (
    @EmbeddedId
    var id: ApplicationID?
)

