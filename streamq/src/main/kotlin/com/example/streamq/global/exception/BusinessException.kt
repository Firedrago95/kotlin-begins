package com.example.streamq.global.exception

open class BusinessException (val errorCode: ErrorCode) : RuntimeException(errorCode.message)
