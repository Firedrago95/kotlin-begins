package com.example.streamq.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class OpenAiStreamResponse(
    val choices: List<StreamChoice>
)

data class StreamChoice(
    val delta: Delta,
    @JsonProperty("finish_reason")
    val finishReason: String?
)

data class Delta(
    val content: String?
)
