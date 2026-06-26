package com.example.analyzer.exception

// [미션 1] 코틀린의 Sealed Class를 활용하여 예외 계층을 직접 설계해 보세요!
// 힌트: sealed class AiApiException(message: String) : RuntimeException(message)

// TODO 1: 부모가 될 AiApiException 을 sealed class로 선언하세요.
sealed class AiApiException(message: String) : RuntimeException(message)

// TODO 2: 일시적인 서버 장애(Transient)를 나타내는 예외로 수정 (5xx 에러 등, 클라이언트 단에서 재시도의 기준이 되며 최종 실패 시 서비스 계층에서 폴백 판단의 근거가 됨)
class TransientApiException(message: String) : AiApiException(message)

// TODO 3: 재시도가 무의미한 예외(NonRetryableApiException)를 만드세요. (4xx 에러용)
class NonRetryableApiException(message: String) : AiApiException(message)

// TODO 4: 전혀 예상치 못한 예외(UnexpectedApiException)를 만드세요.
class UnexpectedApiException(message: String) : AiApiException(message)
