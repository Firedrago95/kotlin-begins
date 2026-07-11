package com.example.streamq.global.exception

import java.time.ZonedDateTime

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String?,
    val timestamp: ZonedDateTime = ZonedDateTime.now()
) {
    companion object {
        fun of(errorCode: ErrorCode): ErrorResponse {
            return ErrorResponse(
                status = errorCode.status.value(),
                error = errorCode.name,
                message = errorCode.message
            )
        }

        fun of(errorCode: ErrorCode, message: String?): ErrorResponse {
            return ErrorResponse(
                status = errorCode.status.value(),
                error = errorCode.name,
                message = message ?: errorCode.message
            )
        }
    }
}
