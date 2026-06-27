package com.example.analyzer.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AiApiException::class)
    fun handleAiApiException(e: AiApiException): ResponseEntity<String> {
        // 코틀린의 Sealed Class와 when 구문을 활용한 완벽한 분기 처리
        val status = when (e) {
            is TransientApiException -> HttpStatus.SERVICE_UNAVAILABLE // 503
            is NonRetryableApiException -> HttpStatus.BAD_REQUEST // 400
            is UnexpectedApiException -> HttpStatus.INTERNAL_SERVER_ERROR // 500
        }
        
        return ResponseEntity.status(status).body("AI 분석 중 에러가 발생했습니다: ${e.message}")
    }
}
