package com.example.streamq.domain.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Entity
@Table(name="users")
@EntityListeners(AuditingEntityListener::class)
class User (
    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    var passwordHash: String,

    @Column(nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    var role: Role = Role.MEMBER,
){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @CreatedDate
    @Column(updatable = false)
    var createdAt: ZonedDateTime? = null

    @LastModifiedBy
    var updatedAt: ZonedDateTime? = null
}
