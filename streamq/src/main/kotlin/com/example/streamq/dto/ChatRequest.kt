package com.example.streamq.dto

data class ChatRequest(
    val content: String,
    val model : String = "gpt-3.5-turbo",
    val isStreaming: Boolean = true
)
