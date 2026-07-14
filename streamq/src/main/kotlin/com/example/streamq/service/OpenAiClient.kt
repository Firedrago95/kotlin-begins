package com.example.streamq.service

import com.example.streamq.dto.AiMessageDto
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import org.springframework.http.codec.ServerSentEvent
import org.springframework.core.ParameterizedTypeReference
import com.example.streamq.dto.OpenAiStreamResponse
import org.slf4j.LoggerFactory

@Component
class OpenAiClient(
    private val webClientBuilder: WebClient.Builder,
    private val objectMapper: ObjectMapper,
    @Value("\${openai.api-key:dummy-api-key}") private val apiKey: String,
    @Value("\${openai.url:https://api.openai.com/v1/chat/completions}") private val openAiUrl: String
) : AiClient {
    private val log = LoggerFactory.getLogger(OpenAiClient::class.java)

    override fun askStreaming(messages: List<AiMessageDto>, model: String): Flux<StreamEvent> {
        val requestBody = mapOf(
            "model" to model,
            "messages" to messages,
            "stream" to true
        )

        return webClientBuilder.build()
            .post()
            .uri(openAiUrl)
            .header("Authorization", "Bearer $apiKey")
            .bodyValue(requestBody)
            .retrieve()
            .onStatus({ it.is4xxClientError }) { response ->
                val errorCode = when (response.statusCode()) {
                    org.springframework.http.HttpStatus.UNAUTHORIZED -> com.example.streamq.global.exception.ErrorCode.OPENAI_UNAUTHORIZED
                    org.springframework.http.HttpStatus.TOO_MANY_REQUESTS -> com.example.streamq.global.exception.ErrorCode.OPENAI_RATE_LIMIT
                    else -> com.example.streamq.global.exception.ErrorCode.EXTERNAL_API_CLIENT_ERROR
                }
                reactor.core.publisher.Mono.error(com.example.streamq.global.exception.ExternalApiException(errorCode))
            }
            .onStatus({ it.is5xxServerError }) { _ ->
                reactor.core.publisher.Mono.error(com.example.streamq.global.exception.ExternalApiException(com.example.streamq.global.exception.ErrorCode.OPENAI_SERVER_ERROR))
            }
            .bodyToFlux(object : ParameterizedTypeReference<ServerSentEvent<String>>() {})
            .timeout(java.time.Duration.ofSeconds(60))
            .filter { !it.data().isNullOrBlank() }
            .takeWhile { it.data() != "[DONE]" }
            .mapNotNull<StreamEvent> { sse ->
                try {
                    val response = objectMapper.readValue(sse.data(), OpenAiStreamResponse::class.java)
                    val choice = response.choices.firstOrNull() ?: return@mapNotNull null
                    if (choice.finishReason == "content_filter") {
                        StreamEvent.Filtered("content_filter")
                    } else if (!choice.delta.content.isNullOrEmpty()) {
                        StreamEvent.Content(choice.delta.content)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    log.warn("OpenAI SSE JSON parsing failed: {}", sse.data(), e)
                    null
                }
            }
    }

    override fun askSync(messages: List<AiMessageDto>, model: String): reactor.core.publisher.Mono<String> {
        val requestBody = mapOf(
            "model" to model,
            "messages" to messages,
            "stream" to false
        )

        return webClientBuilder.build()
            .post()
            .uri(openAiUrl)
            .header("Authorization", "Bearer $apiKey")
            .bodyValue(requestBody)
            .retrieve()
            .onStatus({ it.is4xxClientError }) { response ->
                val errorCode = when (response.statusCode()) {
                    org.springframework.http.HttpStatus.UNAUTHORIZED -> com.example.streamq.global.exception.ErrorCode.OPENAI_UNAUTHORIZED
                    org.springframework.http.HttpStatus.TOO_MANY_REQUESTS -> com.example.streamq.global.exception.ErrorCode.OPENAI_RATE_LIMIT
                    else -> com.example.streamq.global.exception.ErrorCode.EXTERNAL_API_CLIENT_ERROR
                }
                reactor.core.publisher.Mono.error(com.example.streamq.global.exception.ExternalApiException(errorCode))
            }
            .onStatus({ it.is5xxServerError }) { _ ->
                reactor.core.publisher.Mono.error(com.example.streamq.global.exception.ExternalApiException(com.example.streamq.global.exception.ErrorCode.OPENAI_SERVER_ERROR))
            }
            .bodyToMono(String::class.java)
            .mapNotNull<String> { jsonString -> parseSyncResponse(jsonString) }
            .defaultIfEmpty("")
    }

    private fun parseSyncResponse(jsonString: String): String? {
        return try {
            val rootNode = objectMapper.readTree(jsonString)
            rootNode.path("choices").get(0)?.path("message")?.path("content")?.asText()
        } catch (e: JsonProcessingException) {
            null
        }
    }
}
