package com.example.animation.controller.dto.request

data class PromptRequest(
    val prompt: String,
    val idempotencyKey: String
)
