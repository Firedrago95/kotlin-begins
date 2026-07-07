package com.example.streamq.domain.feedback

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.JpaRepository

interface FeedbackRepository : JpaRepository<Feedback, Long> {
    fun findByUserId(userId: Long, pageable: Pageable): Page<Feedback>
    fun findByUserIdAndIsPositive(userId: Long, isPositive: Boolean, pageable: Pageable): Page<Feedback>
    fun findByIsPositive(isPositive: Boolean, pageable: Pageable): Page<Feedback>

    @Modifying
    @Query("DELETE FROM Feedback f WHERE f.aiChat.thread.id = :threadId")
    fun deleteAllByThreadId(threadId: Long)
}
