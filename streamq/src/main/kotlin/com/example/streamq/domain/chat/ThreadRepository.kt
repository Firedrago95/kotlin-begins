package com.example.streamq.domain.chat

import org.springframework.data.jpa.repository.JpaRepository

import org.springframework.data.domain.Pageable
import java.util.Optional

interface ThreadRepository : JpaRepository<Thread, Long> {
    fun findTopByUserIdOrderByUpdatedAtDesc(userId: Long): Thread?
}
