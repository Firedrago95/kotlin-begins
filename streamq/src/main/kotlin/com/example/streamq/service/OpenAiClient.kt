package com.example.streamq.service

import com.example.streamq.dto.AiMessageDto
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

@Component
class OpenAiClient(
    private val webClientBuilder: WebClient.Builder,
    private val objectMapper: ObjectMapper,
    @Value("\${openai.api-key:dummy-api-key}") private val apiKey: String,
    @Value("\${openai.url:https://api.openai.com/v1/chat/completions}") private val openAiUrl: String
) : AiClient {

    override fun askStreaming(messages: List<AiMessageDto>, model: String): Flux<String> {
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
            .bodyToFlux(String::class.java)
            .timeout(java.time.Duration.ofSeconds(60))
            .filter { it.isNotBlank() }
            .map { it.removePrefix("data: ").trim() }
            .takeWhile { it != "[DONE]" }
            .mapNotNull<String> { cleanChunk -> parseStreamingChunk(cleanChunk) }
            .filter { it.isNotEmpty() }
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
            .bodyToMono(String::class.java)
            .mapNotNull<String> { jsonString -> parseSyncResponse(jsonString) }
            .defaultIfEmpty("")
    }

    private fun parseStreamingChunk(cleanChunk: String): String? {
        if (!cleanChunk.startsWith("{")) return null
        return try {
            val rootNode = objectMapper.readTree(cleanChunk)
            rootNode.path("choices").get(0)?.path("delta")?.path("content")?.asText()
        } catch (e: JsonProcessingException) {
            null
        }
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
