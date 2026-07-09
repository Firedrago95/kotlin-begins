package com.example.streamq.global.exception

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("예상치 못한 예외 발생", e)
        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR
        val errorResponse = ErrorResponse(errorCode = errorCode.code, message = errorCode.message)
        return ResponseEntity.status(errorCode.status).body(errorResponse)
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode
        val errorResponse = ErrorResponse(errorCode = errorCode.code, message = errorCode.message)
        return ResponseEntity.status(errorCode.status).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        log.warn("DTO 유효성 검사 실패: {}", e.message)
        val errorMessage = e.bindingResult.allErrors.firstOrNull()?.defaultMessage ?: ErrorCode.INVALID_INPUT_VALUE.message
        val errorCode = ErrorCode.INVALID_INPUT_VALUE
        val errorResponse = ErrorResponse(errorCode = errorCode.code, message = errorMessage)
        return ResponseEntity.status(errorCode.status).body(errorResponse)
    }
}
