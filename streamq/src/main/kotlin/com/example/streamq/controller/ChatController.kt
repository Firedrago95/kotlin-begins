package com.example.streamq.controller

import com.example.streamq.dto.ChatRequest
import com.example.streamq.service.ChatAppService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/v1/chats")
class ChatController (
    private val chatAppService: ChatAppService
) {

    @PostMapping
    fun createChat(
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: ChatRequest
    ): ResponseEntity<Flux<String>> {
        val mediaType = if (request.isStreaming) {
            MediaType.TEXT_EVENT_STREAM
        } else {
            MediaType.APPLICATION_JSON
        }

        return ResponseEntity.ok()
            .contentType(mediaType)
            .body(chatAppService.createChat(userId, request))
    }
}
