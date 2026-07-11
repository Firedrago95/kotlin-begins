package com.example.streamq.service

sealed interface StreamEvent {
    data class Content(val text: String) : StreamEvent
    data class Filtered(val reason: String) : StreamEvent
}
