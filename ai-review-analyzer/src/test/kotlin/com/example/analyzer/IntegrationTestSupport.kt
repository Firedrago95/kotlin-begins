package com.example.analyzer

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
abstract class IntegrationTestSupport {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:15.8").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }
    }
}
