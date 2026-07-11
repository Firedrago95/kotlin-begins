package com.example.streamq.global.exception

class ExternalApiException(
    errorCode: ErrorCode,
    message: String? = errorCode.message
) : BusinessException(errorCode, message)
