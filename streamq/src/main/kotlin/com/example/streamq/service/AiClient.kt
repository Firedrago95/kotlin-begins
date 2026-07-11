package com.example.streamq.service

import com.example.streamq.dto.AiMessageDto

interface AiClient {
    // 1. 스트리밍
    fun askStreaming(messages: List<AiMessageDto>, model: String)

    // 2. 비스트리밍
    fun askSync(messages: List<AiMessageDto>, model: String)
}
