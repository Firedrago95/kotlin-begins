package com.example.streamq.domain.chat

import com.example.streamq.domain.user.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime

@Entity
@Table(
    name = "threads",
    indexes = [
        Index(name = "idx_user_updated", columnList = "user_id, updated_at DESC")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class Thread(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    var createdAt: ZonedDateTime? = null

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    var updatedAt: ZonedDateTime? = null

    fun isExpired(now: ZonedDateTime): Boolean {
        if (updatedAt == null) return false
        return updatedAt!!.plusMinutes(30).isBefore(now)
    }
}
