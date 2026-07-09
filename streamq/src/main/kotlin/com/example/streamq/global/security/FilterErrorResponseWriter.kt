package com.example.streamq.global.security

import com.example.streamq.global.exception.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType

object FilterErrorResponseWriter {
    fun write(
        response: HttpServletResponse,
        status: Int,
        code: String,
        message: String,
        objectMapper: ObjectMapper
    ) {
        response.status = status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(objectMapper.writeValueAsString(ErrorResponse(errorCode = code, message = message)))
    }
}
