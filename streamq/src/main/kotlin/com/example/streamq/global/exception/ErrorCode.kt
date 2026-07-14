package com.example.streamq.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val message: String
) {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호가 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    THREAD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 대화 스레드를 찾을 수 없습니다."),
    THREAD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 스레드에 접근(삭제)할 권한이 없습니다."),
    CHAT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 채팅을 찾을 수 없습니다."),
    INVALID_CHAT_STATUS(HttpStatus.BAD_REQUEST, "완료된 AI 대화에만 피드백을 남길 수 있습니다."),
    FEEDBACK_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 이 대화에 피드백을 남기셨습니다."),
    FEEDBACK_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 대화에 피드백을 남길 권한이 없습니다."),
    FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 피드백을 찾을 수 없습니다."),
    
    // 외부 API 관련 에러 (재시도 목적이 아닌 '에러 원인 식별' 목적)
    EXTERNAL_API_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "잘못된 프롬프트 입력이거나 AI 정책을 위반했습니다."),
    OPENAI_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "OpenAI API 인증에 실패했습니다."),
    OPENAI_RATE_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "OpenAI API 호출 한도를 초과했습니다."),
    OPENAI_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "OpenAI 서버 연동 중 오류가 발생했습니다."),
    EXTERNAL_API_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "외부 AI 서비스 연동 중 오류가 발생했습니다."),
    
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.")
}
