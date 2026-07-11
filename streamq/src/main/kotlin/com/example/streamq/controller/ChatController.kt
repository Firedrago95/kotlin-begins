package com.example.streamq.controller

import com.example.streamq.dto.ChatRequest
import com.example.streamq.dto.ChatSyncResponse
import com.example.streamq.service.ChatAppService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/chats")
class ChatController (
    private val chatAppService: ChatAppService
) {

    @PostMapping
    fun createChat(
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: ChatRequest
    ): Mono<ResponseEntity<*>> {
        return if (request.isStreaming) {
            Mono.just(
                ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(chatAppService.createChatStream(userId, request))
            )
        } else {
            chatAppService.handleSyncChat(userId, request)
                .map { content ->
                    ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(ChatSyncResponse(content))
                }
        }
    }
}
