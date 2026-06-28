package com.example.analyzer.exception

import com.example.analyzer.presentation.dto.ExceptionResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AiApiException::class)
    fun handleAiApiException(e: AiApiException): ResponseEntity<ExceptionResponse> {
        val errorCode = e.errorCode
        
        return ResponseEntity
            .status(errorCode.status)
            .body(ExceptionResponse.of(errorCode))
    }
}
