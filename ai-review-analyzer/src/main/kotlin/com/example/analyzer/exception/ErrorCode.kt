package com.example.analyzer.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // AI 통신 관련 에러
    AI_SERVER_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "AI-001", "AI 서버가 일시적으로 지연되고 있습니다. 잠시 후 다시 시도해주세요."),
    AI_BAD_REQUEST(HttpStatus.BAD_REQUEST, "AI-002", "잘못된 분석 요청입니다. 입력값을 확인해주세요."),
    AI_UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI-003", "AI 분석 중 알 수 없는 에러가 발생했습니다."),

    // 기타 서버 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS-001", "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.")
}
