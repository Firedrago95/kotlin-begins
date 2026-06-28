package com.example.analyzer.exception

// 1. 부모 클래스를 봉인(sealed)합니다. (ErrorCode를 필수로 받도록 수정)
sealed class AiApiException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)

// 2. 자식 클래스들은 AiApiException을 상속받으며, 기본 ErrorCode를 할당합니다.
// 일시적인 서버 장애(Transient)를 나타내는 예외
class TransientApiException(errorCode: ErrorCode = ErrorCode.AI_SERVER_ERROR) : AiApiException(errorCode)

// 재시도가 무의미한 예외 (4xx 에러용)
class NonRetryableApiException(errorCode: ErrorCode = ErrorCode.AI_BAD_REQUEST) : AiApiException(errorCode)

// 전혀 예상치 못한 예외
class UnexpectedApiException(errorCode: ErrorCode = ErrorCode.AI_UNEXPECTED_ERROR) : AiApiException(errorCode)
