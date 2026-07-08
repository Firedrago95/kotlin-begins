package com.example.streamq.global.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode
        val errorResponse = ErrorResponse(errorCode = errorCode.code, message = errorCode.message)
        return ResponseEntity.status(errorCode.status).body(errorResponse)
    }
}
