package com.example.streamq.service

import com.example.streamq.dto.AiMessageDto
import com.example.streamq.dto.ChatRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SignalType
import reactor.core.scheduler.Schedulers

@Service
class ChatAppService(
    private val chatDomainService: ChatDomainService,
    private val aiClient: AiClient
) {
    private val log = LoggerFactory.getLogger(ChatAppService::class.java)

    fun createChat(userId: Long, request: ChatRequest): Flux<String> {
        if (!request.isStreaming) {
            return handleSyncChat(userId, request).flux()
        }

        // 스트리밍 처리
        // 1. 블로킹 작업을 boundedElastic 에게 던짐
        return Mono.fromCallable {
            val (userChat, aiChat) = chatDomainService.prepareChats(userId, request.content)
            val history = chatDomainService.getChatHistory(userChat.thread.id, aiChat.id)
            val aiMessages = history.map {
                AiMessageDto(
                    role = if (it.javaClass.simpleName == "UserChat") "user" else "assistant",
                    content = it.content
                )
            }
            Pair(aiChat.id, aiMessages)
        }
        .subscribeOn(Schedulers.boundedElastic())
        // 2. 준비된 데이터로 OpenAI 물줄기(Flux) 연결
        .flatMapMany { (aiChatId, aiMessages) ->
            val responseBuilder = StringBuilder()

            aiClient.askStreaming(aiMessages, request.model)
                .doOnNext { chunk -> responseBuilder.append(chunk) }
                .doFinally { signalType ->
                    Mono.fromRunnable<Void> {
                        val finalContent = responseBuilder.toString()
                        if (signalType == SignalType.ON_COMPLETE) {
                            chatDomainService.updateAiChatSuccess(aiChatId, finalContent)
                        } else if (signalType == SignalType.CANCEL) {
                            chatDomainService.updateAiChatPartial(aiChatId, finalContent)
                        } else {
                            chatDomainService.updateAiChatFailed(aiChatId)
                        }
                    }
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe({}, { error -> log.error("chat status update failed", error)})
                }
        }
    }

    private fun handleSyncChat(userId: Long, request: ChatRequest): Mono<String> {
        return Mono.fromCallable {
            val (userChat, aiChat) = chatDomainService.prepareChats(userId, request.content)
            val history = chatDomainService.getChatHistory(userChat.thread.id, aiChat.id)
            val aiMessages = history.map {
                AiMessageDto (
                    role = if (it.javaClass.simpleName == "UserChat") "user" else "assistant",
                    content = it.content
                )
            }
            Pair(aiChat.id, aiMessages)
        }
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap { (aiChatId, aiMessages) ->
            aiClient.askSync(aiMessages, request.model)
                .flatMap { finalContent ->
                    Mono.fromRunnable<Void> {
                        chatDomainService.updateAiChatSuccess(aiChatId, finalContent)
                    }.subscribeOn(Schedulers.boundedElastic())
                    .thenReturn(finalContent)
                }
                .onErrorResume { error ->
                    Mono.fromRunnable<Void> {
                        chatDomainService.updateAiChatFailed(aiChatId)
                    }.subscribeOn(Schedulers.boundedElastic())
                    .then(Mono.error(error))
                }
        }
    }
}
