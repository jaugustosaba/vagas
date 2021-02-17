package br.com.vagas.application.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext
@ExtendWith(SpringExtension::class)
class DistanceTest {
    @Autowired
    lateinit var service: VagasService

    @Autowired
    lateinit var locationRepo: LocationRepository

    @Autowired
    lateinit var distanceRepo: DistanceRepository

    @Test
    fun testDistances() {
        fun location(name: String): Location =
            locationRepo.save(Location(id = null, name = name))

        fun distance(from: String, to: String, d: Int) {
            val fromObj = locationRepo.findByName(from) ?: throw Exception("cannot find $from")
            val toObj = locationRepo.findByName(to) ?: throw Exception("cannot find $to")
            distanceRepo.save(Distance(id= DistanceId(from_location = fromObj, to_location = toObj), d))
        }

        location("A")
        location("B")
        location("C")
        location("D")
        location("E")
        location("F")

        distance("A", "B", 5)
        distance("B", "C", 7)
        distance("B", "D", 3)
        distance("C", "E", 4)
        distance("D", "E", 10)
        distance("D", "F", 8)

        // one step distances
        Assertions.assertEquals(5, service.distance("A", "B"))
        Assertions.assertEquals(7, service.distance("B", "C"))
        Assertions.assertEquals(3, service.distance("B", "D"))
        Assertions.assertEquals(4, service.distance("C", "E"))
        Assertions.assertEquals(10, service.distance("D", "E"))
        Assertions.assertEquals(8, service.distance("D", "F"))

        // multiple step distances
        Assertions.assertEquals(16, service.distance("A", "E"))
        Assertions.assertEquals(18, service.distance("C", "F"))
    }

}