# 1. 아키텍처 및 구현 설계 (Architecture & Implementation Design)

이 문서는 StreamQ 프로젝트의 데이터베이스 스키마, REST API, 그리고 실무적인 세부 구현 노하우와 그 '근거'를 통합하여 기록합니다.

## 1. DB 스키마 및 엔티티 설계
- **상속 전략**: UserChat과 AiChat을 분리하지 않고 `SINGLE_TABLE` 전략을 선택한 이유
- **인덱스 및 관계**: 명시적 외래키 제약조건을 피하고 논리적 연관관계와 Index만 활용한 실무적 이유

## 2. API 및 통신 규격 설계
- **SSE(Server-Sent Events)**: WebSocket이 아닌 SSE를 선택한 이유와 `TEXT_EVENT_STREAM_VALUE` 응답 구조
- **RESTful 네이밍**: 피드백 상태 변경을 `PUT`이 아닌 `PATCH /feedbacks/{id}/resolve` 동사형 믹스로 설계한 이유
- **페이징 전략**: 대화 목록의 커서(Cursor) 방식과 피드백의 오프셋(Offset) 방식 믹스 적용기

## 3. 동시성 제어 및 엣지 케이스 방어
- **레이스 컨디션**: 피드백 테이블의 복합 유니크 인덱스(`uk_chat_user`) 적용기
- **멱등성**: `DataIntegrityViolationException`을 활용한 멱등성 보장 및 409 Conflict 예외 처리 원리
