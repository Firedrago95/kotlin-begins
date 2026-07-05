package com.example.animation.domain

import jakarta.persistence.*

@Entity
@Table(
    name = "cuts",
    indexes = [Index(name = "idx_cut_status", columnList = "status")]
)
class Cut(
    @Column(nullable = false)
    val sceneId: Long,

    @Column(columnDefinition = "TEXT", nullable = false)
    val imagePrompt: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    val videoPrompt: String,

    @Column(nullable = false)
    val durationSec: Int = 10,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: GenerationStatus = GenerationStatus.PENDING,

    @Column(columnDefinition = "TEXT")
    var failureReason: String? = null,

    @Version
    var version: Long = 0L,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L
) {
    // TODO: 코틀린의 require()를 활용한 상태 변경(전이) 로직 작성 필요
}
