# AI Handoff Context (StreamQ Project)

이 문서는 사용자가 새로운 AI 세션에서 작업을 매끄럽게 이어가기 위한 맥락 전달용(Handoff) 파일입니다.
새로운 AI 어시스턴트는 이 문서를 읽고 사용자의 목표, 현재 진행 상태, 그리고 엄격한 협업 규칙을 즉시 파악해야 합니다.

## 1. 현재 상황 및 프로젝트 목적
*   **배경**: 기존에 완성된 'AI 챗봇 시연용 API 과제'(`mission` 패키지) 코드는 제출용 레퍼런스로 보존합니다. 현재 위치한 새 디렉토리(`kotlin-begins/streamq`)에서 완전히 백지부터 다시 코딩하며 제대로 된 학습을 진행하는 프로젝트입니다.
*   **궁극적 목표**: 
    1. **실무 구현력 향상**: DB 테이블 스키마 설계, REST API 동사/명사 규칙, 동시성 제어 노하우 체화.
    2. **심층 이론 딥다이브**: WebFlux, Netty Event Loop, OS 커널 레벨의 Non-blocking I/O(epoll)와 Context Switching에 대한 완벽한 이해. (심층 면접 완벽 방어 수준)
*   **컨셉 리브랜딩**: "실시간 스트리밍 답변을 제공하는 고객 지원(CS) AI 어시스턴트 API (StreamQ)"

## 2. 파일 구조 및 문서화 전략 (역할 분담)
*   **프로젝트 경로**: `/Users/iyonghwa/kotlin-begins/streamq` (원본 코드는 `../mission`에 위치하며 절대 수정 불가)
*   **문서 기록의 엄격한 경계 (`docs/`)**:
    *   `1_IMPLEMENTATION_DESIGN.md`: **[사용자 전용 공간]** 아키텍처, DB 설계 근거, REST API URL 규칙 등 프로젝트의 '구현 노하우와 설계 방향'을 사용자가 직접 수기로 기록하는 공간입니다. **AI는 이 파일을 절대 자동으로 수정해선 안 됩니다.**
    *   `2_DEEP_DIVE.md`: **[사용자 전용 공간]** 사용자가 학습을 위해 100% 수기로 직접 깨달은 점을 기록하는 공간입니다. **AI는 이 파일을 절대 자동으로 수정해선 안 됩니다.**
    *   `3_ORIGINAL_CODE_SUMMARY.md`: **[AI 전용 공간]** AI가 원본 코드(`mission` 패키지)를 분석한 내용, 복잡한 WebFlux/JPA 믹스 기술 등에 대해 스스로 기억하고 힌트를 주기 위해 사용하는 맥락 유지용 파일입니다. AI의 지식 정리는 오직 이곳에만 남깁니다.
    *   `원본_요구사항.md`: 원래 과제의 명세 백업.

## 3. 진행할 커리큘럼 (4단계)
*   **Phase 1: 뼈대 설계 (API & DB 모델링)**: User, Thread, Chat, Feedback 테이블 설계 및 REST API 명세 토론.
*   **Phase 2: WebFlux와 Non-blocking I/O**: `WebClient`를 이용한 OpenAI SSE 스트리밍 연동. 로그를 통한 스레드 생명주기 추적.
*   **Phase 3: 극한의 충돌 (JPA vs WebFlux)**: 의도적으로 스트리밍 중 JPA I/O를 발생시켜 메인 스레드를 뻗게 만들고, `boundedElastic` 오프로딩(Offloading)으로 서버를 고치는 실증 과정.
*   **Phase 4: 동시성 방어와 완성**: 피드백 생성 시 유니크 제약 조건을 활용한 멱등성 보장 및 409 Conflict 처리.

## 4. 새 AI 어시스턴트에게 주는 행동 지침 (필독 협업 규칙)
1.  **정답 코드부터 뱉지 않기**: 사용자가 '스스로 구현력을 기르는 것'이 핵심 목적입니다. 무작정 코드를 주지 말고, 어떤 테이블 구조가 좋은지, URL은 어떻게 짤 것인지 먼저 질문하고 단계적으로 토론을 유도하세요.
2.  **이론적 깊이 유지**: WebFlux 관련 로직을 작성할 때는 항상 OS의 Thread Context Switching, Epoll, Tomcat vs Netty의 차이 관점에서 깊이 있는 설명을 곁들이세요.
3.  **기록 및 문서화 경계 철저히 지키기**: AI 스스로 기억해야 할 구현 내용이나 구조 요약은 **반드시 `3_ORIGINAL_CODE_SUMMARY.md`에만** 기록하세요. 사용자의 수기 학습 공간인 `1_IMPLEMENTATION_DESIGN.md`와 `2_DEEP_DIVE.md`는 절대 침범하지 마세요.
4.  **원본 보존**: 힌트를 위해 원본 제출본(`mission` 패키지)을 열람하고 참고하는 것은 허용되나, **절대 원본 패키지의 코드를 수정해서는 안 됩니다.**
