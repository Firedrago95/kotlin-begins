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
import jakarta.persistence.UniqueConstraint
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Entity
@Table(
    name="users",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_provider_provider_id", columnNames = ["provider", "provider_id"])
    ]
)
@EntityListeners(AuditingEntityListener::class)
class User (
    @Column(nullable = false)
    val email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: Provider,

    @Column(nullable = false)
    val providerId: String,

    @Column(nullable = false)
    var name: String,

    var picture: String? = null,

    @Enumerated(EnumType.STRING)
    var role: Role = Role.MEMBER,
){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @CreatedDate
    @Column(updatable = false)
    var createdAt: ZonedDateTime? = null

    @LastModifiedDate
    var updatedAt: ZonedDateTime? = null
}
