package com.example.streamq.domain.chat

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Pageable

interface ThreadRepository : JpaRepository<Thread, Long> {
    fun findTopByUserIdOrderByUpdatedAtDesc(userId: Long): Thread?

    // Member: cursor desc
    fun findByUserIdAndIdLessThanOrderByIdDesc(userId: Long, id: Long, pageable: Pageable): List<Thread>
    fun findByUserIdOrderByIdDesc(userId: Long, pageable: Pageable): List<Thread>

    // Admin: cursor desc
    fun findByIdLessThanOrderByIdDesc(id: Long, pageable: Pageable): List<Thread>
    fun findAllByOrderByIdDesc(pageable: Pageable): List<Thread>
}
