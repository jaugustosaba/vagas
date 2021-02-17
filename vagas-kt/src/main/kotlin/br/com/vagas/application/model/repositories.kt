package br.com.vagas.application.model

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LocationRepository: CrudRepository<Location, Long> {
    fun findByName(name: String): Location?
}

@Repository
interface DistanceRepository: CrudRepository<Distance, DistanceId> {
    @Query("SELECT d FROM Distance d WHERE d.id.from_location = :from OR d.id.to_location = :from")
    fun findAllNeighbors(@Param("from") from: Location): List<Distance>
}

@Repository
interface LevelRepository: CrudRepository<Level, Long>

@Repository
interface ApplicantRepository: CrudRepository<Applicant, Long>

@Repository
interface JobRepository: CrudRepository<Job, Long>

@Repository
interface ApplicationRepository: CrudRepository<Application, ApplicationID> {

}