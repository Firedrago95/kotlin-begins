package com.example.streamq

import jakarta.persistence.EntityListeners
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class StreamqApplication

fun main(args: Array<String>) {
    runApplication<StreamqApplication>(*args)
}
