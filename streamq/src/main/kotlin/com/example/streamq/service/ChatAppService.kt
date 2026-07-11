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

    fun createChatStream(userId: Long, request: ChatRequest): Flux<String> {
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
            var isFiltered = false

            aiClient.askStreaming(aiMessages, request.model)
                .doOnNext { event ->
                    when (event) {
                        is StreamEvent.Content -> responseBuilder.append(event.text)
                        is StreamEvent.Filtered -> {
                            responseBuilder.append("\n\n[안내: 일부 내용이 OpenAI 정책에 의해 제한되었습니다.]")
                            isFiltered = true
                        }
                    }
                }
                .map { event ->
                    when (event) {
                        is StreamEvent.Content -> event.text
                        is StreamEvent.Filtered -> "\n\n[안내: 일부 내용이 OpenAI 정책에 의해 제한되었습니다.]"
                    }
                }
                .onErrorResume { error ->
                    if (responseBuilder.isEmpty()) {
                        // 첫 청크를 받기 전(스트리밍 커밋 전) 발생한 에러 -> GlobalExceptionHandler가 잡을 수 있게 위로 던짐
                        Mono.error(error)
                    } else {
                        // 이미 스트리밍이 시작된 후 발생한 에러 -> HTTP 상태를 바꿀 수 없으므로 SSE 인밴드 이벤트로 에러 발송
                        log.error("Streaming failed mid-flight", error)
                        Flux.just("event: error\ndata: 응답 생성 중 오류가 발생했습니다.\n\n")
                    }
                }
                .doFinally { signalType ->
                    Mono.fromRunnable<Void> {
                        val finalContent = responseBuilder.toString()
                        if (isFiltered) {
                            chatDomainService.updateAiChatFiltered(aiChatId, finalContent)
                        } else if (signalType == SignalType.ON_COMPLETE) {
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

    fun handleSyncChat(userId: Long, request: ChatRequest): Mono<String> {
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
