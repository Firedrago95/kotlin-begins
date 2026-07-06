package com.example.streamq.domain.chat

import com.example.streamq.domain.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime

@Entity
@Table(name="threads")
@EntityListeners(AuditingEntityListener::class)
class Thread (
    @ManyToOne(fetch = FetchType.LAZY)
    val user: User
){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @CreatedDate
    @Column(updatable = false)
    var createdAt: ZonedDateTime? = null

    @LastModifiedDate
    var updatedAt: ZonedDateTime? = null

    fun isExpired(now: ZonedDateTime): Boolean {
        return updatedAt?.plusMinutes(30)?.isBefore(now) ?: false
    }
}
