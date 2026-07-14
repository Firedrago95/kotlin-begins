package com.example.streamq.service

import com.example.streamq.dto.AiMessageDto

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AiClient {
    // 1. 스트리밍
    fun askStreaming(messages: List<AiMessageDto>, model: String): Flux<StreamEvent>

    // 2. 비스트리밍
    fun askSync(messages: List<AiMessageDto>, model: String): Mono<String>
}
