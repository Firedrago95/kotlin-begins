package com.example.streamq.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode (
    val status: HttpStatus,
    val code: String,
    val message: String
){
    // --- 공통 (Common)---
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),

    // --- 유저 (User) ---
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 가입된 이메일입니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "U003", "이메일 혹은 비밀번호가 틀렸습니다.")
}
