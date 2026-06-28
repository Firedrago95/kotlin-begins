package com.example.analyzer.client

import com.example.analyzer.exception.NonRetryableApiException
import com.example.analyzer.exception.TransientApiException
import com.example.analyzer.exception.UnexpectedApiException
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

// 1. OpenAI 연동 DTO (Data Class)
data class OpenAiRequest(
    val model: String = "gpt-4o",
    val messages: List<Message>,
    @JsonProperty("response_format")
    val responseFormat: ResponseFormat = ResponseFormat()
)

data class Message(
    val role: String,
    val content: String
)

data class ResponseFormat(val type: String = "json")

data class OpenAiResponse(val choices: List<Choice>)

data class Choice(val message: Message)

// 2. 서비스 내부 사용 응답 객체
data class AiResponse(val sentiment: String, val summary: String)

@Component
class AiApiClient(
    restClientBuilder: RestClient.Builder
) {
    // RestClient 초기화 (기본 BaseUrl 세팅)
    private val restClient: RestClient = restClientBuilder
        .baseUrl("https://api.openai.com/v1")
        .build()

    fun requestAnalysis(content: String): AiResponse {
        // [작성 완료] OpenAiRequest 객체 생성
        val messages = listOf(Message(role = "영화 리뷰어", content = "제로 다크 서티 영화 평가 모아서 정리해줘"))
        val openAiRequest = OpenAiRequest(messages = messages)

        // [작성 완료] RestClient API 체이닝 및 상태 코드별 예외(Sealed Class) 매핑
        val response = restClient.post()
            .uri("/chat/completions")
            .body(openAiRequest)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) { request, response -> throw NonRetryableApiException() }
            .onStatus(HttpStatusCode::is5xxServerError) { request, response -> throw TransientApiException() }
            .body<OpenAiResponse>() ?: throw UnexpectedApiException()

        // [작성 완료] 응답 매핑
        val text = response.choices[0].message.content

        return AiResponse(sentiment = "JOY", summary = text)
    }
}
