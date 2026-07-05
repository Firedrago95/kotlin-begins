package com.example.animation.domain

enum class GenerationStatus {
    PENDING,
    PROCESSING_IMAGE,
    PENDING_VIDEO,
    PROCESSING_VIDEO,
    COMPLETED,
    FAILED
}
