# StreamQ 개발 마일스톤 및 작업 계획 (Tasks)

이 문서는 `streamq` 프로젝트의 전체 개발 흐름을 파악하고, 현재 진행 상황을 트래킹하기 위한 체크리스트입니다.

## Phase 1: 도메인 엔티티 설계 및 리포지토리 (진행 중 🏃‍♂️)
- [x] `User`, `Role` 엔티티 설계 (JPA Auditing 적용)
- [x] `Thread` 엔티티 설계 (연관관계 매핑 및 로직)
- [x] `Chat`, `UserChat`, `AiChat` 엔티티 설계 (싱글 테이블 상속 전략 적용)
- [x] `Feedback`, `FeedbackStatus` 엔티티 설계
- [x] 각 도메인별 Repository 인터페이스 생성 (Spring Data JPA)

## Phase 2: 글로벌 설정 및 보안 (Global & Security) (완료 🚀)
- [x] 전역 예외 처리기 (Global Exception Handler) 구축
- [x] JWT 기반 인증/인가 유틸리티 구현 (`auth` 패키지)
- [x] Spring Security (WebMvc 기반) 설정 적용

## Phase 3: 비즈니스 로직 및 외부 API 연동 (Service Layer)
- [ ] `AuthService`: 회원가입 및 로그인 로직
- [ ] `OpenAiClient`: Spring WebClient를 이용한 OpenAI API 스트리밍 연동
- [ ] `ChatDomainService` & `ChatAppService`: 채팅 저장, 조회, AI 응답 생성 파이프라인
- [ ] `ChatQueryService`: 채팅 내역 조회 (페이징/커서 기반)
- [ ] `FeedbackService`: AI 응답에 대한 좋아요/싫어요 평가 기능

## Phase 4: 프레젠테이션 계층 (Controller & DTO)
- [ ] 전역 DTO 레코드/클래스 생성
- [ ] `AuthController`: 회원가입/로그인 API 엔드포인트
- [ ] `ChatController`: 채팅 전송(SSE 적용) 및 조회 API 엔드포인트
- [ ] `FeedbackController`: 피드백 API 엔드포인트

## Phase 5: 최종 테스트 및 점검
- [ ] 로컬 docker-compose DB 연동 테스트
- [ ] API 테스트 (Postman / IntelliJ HTTP Client)
- [ ] 코드 리팩토링 및 원본 코드와의 최종 대조
