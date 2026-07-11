# 3. 원본 제출본 코드 구현 요약 (Reference)

이 문서는 이전 과제 제출본(`mission` 패키지)의 주요 구현 방식을 요약한 것입니다. 새로운 `streamq` 프로젝트를 백지부터 구현할 때 참고용 및 힌트 제공용으로 사용됩니다.

## 🚨 원본 파일 위치 및 참고(개입) 타이밍 (AI 필독)
*   **원본 파일 위치**: `/Users/iyonghwa/mission` (절대 수정 금지, 오직 Read-only 참고용)
*   **언제 이 원본 코드를 참고하여 개입/조언해야 하는가?**
    1.  사용자가 명시적으로 "힌트를 달라"고 요청할 때.
    2.  사용자가 제안하는 설계나 코드가 **원본 제출본의 방향성과 너무 크게 엇나갈 때**.
        *   *(이유: 추후 과제 전형 면접에서 사용자가 제출한 원본 코드의 설계 의도와 다르게 답변하는 치명적인 상황을 방지하기 위함)*

---

## 1. 도메인 및 엔티티 설계 (JPA)

### User
* `users` 테이블. 
* 필드: `email` (unique), `passwordHash`, `name`, `role` (MEMBER/ADMIN).

### Thread
* `threads` 테이블. 사용자의 대화 세션을 관리.
* `User`와 N:1 연관관계 (`@ManyToOne`).
* 복합 인덱스 사용: `idx_user_updated (user_id, updated_at DESC)` (목록 조회 최적화).
* 핵심 비즈니스 로직: `isExpired(now)` - 마지막 업데이트(`updatedAt`)로부터 30분이 지났는지 판단하여 새로운 스레드를 생성할지 결정.

### Chat (UserChat & AiChat)
* `chats` 테이블. 대화 내역.
* **상속 전략**: `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)` 사용. `role` 컬럼을 Discriminator로 사용하여 `USER`와 `ASSISTANT` 구분.
* `Thread`와 N:1 연관관계. 인덱스 `idx_thread_created (thread_id, created_at ASC)`.
* `AiChat`은 추가적으로 `status` (PENDING, SUCCESS, FAILED 등)와 `parentChat` (`UserChat` 참조 - 어떤 질문에 대한 답변인지)을 가짐.

### Feedback
* `feedbacks` 테이블. AI 답변에 대한 사용자의 평가.
* `User`와 `AiChat`을 각각 N:1 참조.
* **동시성/멱등성 제어**: `UniqueConstraint(name = "uk_chat_user", columnNames = ["chat_id", "user_id"])`를 적용하여 1대화 1유저 1피드백 룰을 DB 레벨에서 강제.

## 2. WebFlux와 JPA의 혼합 (스트리밍 처리) 및 내부 원리 (AI 기억용)

* **WebClient와 Event Loop (Non-Blocking I/O)**: 
  * `OpenAiClient.kt`에서 `WebClient`를 통해 API를 호출할 때, 응답을 대기하느라 스레드를 낭비하지 않습니다. OS 커널(epoll)에 감시를 맡기고, 응답 청크(Chunk)가 올 때만 Netty Event Loop 스레드가 알림을 받아 `Flux`로 흘려보냅니다.
* **Controller와 SSE**: 
  * `createChat` API는 `Flux<String>`을 반환하며, `TEXT_EVENT_STREAM_VALUE` 미디어 타입을 사용하여 Server-Sent Events(SSE)로 응답. 들어오는 청크 데이터를 브라우저로 즉각 Push 합니다.
* **Service (ChatAppService)에서의 충돌 방지 (Offloading)**:
  * **블로킹 I/O 격리**: JPA를 통한 DB 저장(UserChat 및 AiChat Pending 상태 생성, 이전 히스토리 조회)은 동기식(블로킹)이므로, 이것이 Netty 스레드 위에서 실행되면 전체 서버가 마비될 수 있습니다. 이를 방지하기 위해 `Mono.fromCallable`로 감싼 뒤 `.subscribeOn(Schedulers.boundedElastic())`을 통해 무거운 DB 작업을 별도의 임시 스레드 풀로 넘겨(Offloading) 처리합니다.
  * **스트리밍 결합**: 이후 `flatMapMany`를 통해 WebClient 기반의 `aiClient.askStreaming`을 호출하여 비동기 스트림을 리턴함.
  * **스트림 종료 후 상태 업데이트**: `doFinally` 연산자를 활용해 스트리밍이 완료(ON_COMPLETE), 취소(CANCEL), 에러 발생 시 최종 답변 내용을 모아서 다시 `.subscribeOn(Schedulers.boundedElastic())` 환경에서 JPA 상태 업데이트(SUCCESS/FAILED)를 안전하게 수행하여 Netty 스레드를 철저히 보호합니다.

## 3. OAuth2 & JWT 기반 보안 아키텍처 (AI 기억용 추가 내용)

* **CustomOAuth2User & 다중 Provider 확장성**:
  * 구글이 제공하는 식별자(`sub`)를 JWT에 그대로 넣으면 추후 카카오/네이버 연동 시 식별자 충돌 및 비즈니스 로직 분기가 복잡해집니다.
  * 이를 해결하기 위해 `DefaultOAuth2User` 대신 `CustomOAuth2User`를 구현하여 도메인의 진짜 `User` 엔티티를 감쌉니다.
  * SecurityContext에는 우리 DB의 `PK(id)`를 주 식별자로 반환(`getName()`)하도록 하여, 발급되는 JWT의 식별자에는 무조건 내부 `PK`가 들어가도록 설계되었습니다.
* **Dirty Checking (더티 체킹) 활용**:
  * `UserService.getOrRegisterUser()`는 `@Transactional`을 통해 DB에서 회원을 조회하거나 신규 가입(`save()`) 시킵니다.
  * 기존 회원이 로그인할 경우 구글의 최신 프로필로 갱신하는데, `apply {}` 블록으로 값을 재할당하기만 하면 JPA의 변경 감지가 동작하여 명시적인 `save()` 호출 없이 `UPDATE` 쿼리가 발생합니다.
* **OAuth2SuccessHandler**:
  * 인증 성공 시 `CustomOAuth2User`에서 `PK`와 `ROLE_`이 포함된 권한을 꺼내어 자체 JWT Access Token을 발급합니다.
  * 토큰은 `@Value`로 주입된 프론트엔드 URL에 쿼리 파라미터 형태로 리다이렉트되어 전달됩니다. (SPA 환경 실무 타협안 적용)
