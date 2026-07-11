package com.example.streamq.global.exception

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse.of(e.errorCode, e.message)
        return ResponseEntity.status(e.errorCode.status).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Internal Server Error: ", e) // 진짜 에러는 서버 로그에만 기록
        val response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR) // 클라이언트엔 고정 메시지만
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.status).body(response)
    }
}
