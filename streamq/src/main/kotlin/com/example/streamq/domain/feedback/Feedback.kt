package com.example.streamq.domain.feedback

import com.example.streamq.domain.chat.AiChat
import com.example.streamq.domain.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime

@Entity
@Table(
    name = "feedbacks",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_chat_user",
            columnNames = ["chat_id", "user_id"]
        )
    ]
)
@EntityListeners(AuditingEntityListener::class)
class Feedback (
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    val aiChat: AiChat,

    @Column(nullable = false)
    var isPositive: Boolean,
){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Enumerated(value = EnumType.STRING)
    var status: FeedbackStatus = FeedbackStatus.PENDING

    @CreatedDate
    var createdAt: ZonedDateTime? = null

    @LastModifiedDate
    var updatedAt: ZonedDateTime? = null
}
