package com.example.analyzer.presentation.dto

import com.example.analyzer.exception.ErrorCode

data class ExceptionResponse(
    val code: String,
    val message: String
) {
    companion object {
        fun of(errorCode: ErrorCode): ExceptionResponse {
            return ExceptionResponse(
                code = errorCode.code,
                message = errorCode.message
            )
        }
    }
}
