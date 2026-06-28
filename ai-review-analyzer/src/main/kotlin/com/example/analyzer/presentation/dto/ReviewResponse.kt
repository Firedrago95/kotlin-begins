package com.example.analyzer.presentation.dto

data class ReviewResponse(
    val id: Long,
    val sentiment: String,
    val summary: String
)
