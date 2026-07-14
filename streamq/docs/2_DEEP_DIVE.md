# 3. OS 레벨 딥다이브: WebFlux의 진실

이 문서는 코드를 넘어, 애플리케이션 하단(OS/Kernel)에서 일어나는 현상과 Spring WebFlux의 내부 원리를 기록합니다. 심층 면접 대비용 아카이브입니다.

## 1. 스프링 시큐리티 기반 JWT TOKEN 인증 아키텍쳐

### 1. 필터와 SecurityConfig 역할 분리
* **JwtAuthFileter**: 오직 토큰의 유무와 유효성만 검증하고 신분증(Authentication)을 발급하는 역할만 수행한다.
* **SecurityConfig**: 필터를 통과한 요청이 최종적으로 접근할 수 있는 URL 접근제어(`permitAll`, `authenticated`)를 전담한다.

### 2. Access & Refresh 토큰의 분리
보안(XSS, CSRF)과 사용성을 모두 잡기 위해, 두 토큰의 저장 및 전달 방식을 분리하는 구조를 선택한다.
* **Access Token (수명 짧음, 자주 사용)**
  * **저장/전달**: 프론트엔드 메모리에 저장하고, 일반 API 요청 시 HTTP `Authorization: Bearer ` 헤더로 전송한다
  * **장점**: 브라우저가 강제로 전송하지 않으므로 CSRF 공격에 완벽히 안전하다.
  * **단점 방어**: XSS로 탈취당할 위험이 있으나, 토큰의 수명을 짧게 설정하여 탈취 시 타격을 최소화한다.
* **Refresh Token (수명 긺, 재발급에만 쓰임)**
  * **저장/전달**: 로그인 시 서버가 `HttpOnly`, `Secure` 옵션을 걸어 쿠키(Cookie)로 발급한다.
  * **장점**: 자바스크립트로 접근이 불가능하여 XSS 공격으로부터 완벽하게 안전하다. 수명이 긴 토큰을 해커의 탈취로부터 보호할 수 있다.
  * **CSRF 방어**: 쿠키 전송 경로를 특정 API(`Path=/auth/refresh`)로 엄격하게 제한하여, 해커가 악성 사이트에서 일반적인 API 위조 요청을 보내더라도 Refresh Token 쿠키가 동봉되지 않도록 원천 차단한다.

### 3. 스프링 시큐리티 인증 3대장 (사원증 발급 아키텍처)
필터에서 헤더의 Access Token 검증 완료 후, 스프링 시큐리티 규격에 맞는 인증 처리를 위해 아래 3가지 클래스를 사용한다.
1.  **`SimpleGrantedAuthority` (권한 스티커)**: String으로는 권한을 줄 수 없어, 스프링이 요구하는 `GrantedAuthority` 규격에 맞춘 기본 구현체. (주의: `SecurityConfig`에서 `hasRole()`을 사용하기 위해서는 반드시 내부에 `"ROLE_"` prefix가 포함되어 있어야 하므로, 필터에서 안전장치 처리가 필수적이다.)
2.  **`UsernamePasswordAuthenticationToken` (공식 사원증)**: 스프링 시큐리티의 최고 조상인 `Authentication`의 구현체. Principal(사용자 ID), Credentials(비밀번호, 우리는 JWT를 쓰므로 null), Authorities(권한 스티커 목록)를 담는 신분증 역할을 한다. (3-인자 생성자를 사용하면 자동으로 `authenticated = true` 상태가 됨)
3.  **`SecurityContextHolder` (인트라넷 금고)**: 톰캣의 Thread-per-request 모델을 활용해, 현재 요청을 처리 중인 스레드(`ThreadLocal`) 전용 금고에 신분증을 보관한다. 이후 Controller(`@AuthenticationPrincipal`)에서 별도 파싱 없이 유저 정보를 꺼내 쓸 수 있다.

## 2. 구글 로그인

### 1. 스프링 시큐리티와 구글 로그인 흐름
1. **로그인 시작:** '구글 로그인 버튼 클릭' -> `http://{서버 도메인}/oauth2/authorization/google` (공식 시작 주소) 요청
2. **가로채기:** 스프링 시큐리티 최전방 필터 `OAuth2LoginAuthenticationFilter`가 구글 로그인 페이지로 리다이렉트(컨트롤러 필요 x)
3. **콜백수신:** 사용자가 구글 로그인에서 동의 누르면, `http://{서버 도메인}/login/oauth2/code/google` (공식 콜백 주소)로 인증 코드를 전송
4. **또 가로채기:** 최전방 필터가 또 가로채서, 구글 인증 서버와 통신하여 `Acess Token` `ID Token` 바꿔온다.
5. **대리 호출:** 필터가 `CustomOAuth2UserService`(내가 짠 서비스)의 `loadUser()` 호출 이때 내부적으로 `super.loadUser()`가 실행되면서, 넘겨받은 `Access Token`을 들고 구글 프로필 조회 API를 한 번 더 호출하여 진짜 유저 정보(이메일, 이름 등)를 가져옵니다.
6. **마무리:** `loadUser()`에서 DB 저장이 끝나면, 필터는 `OAuth2SuccessHandler`(내가 짠 핸들러) 호출

## 3. WebFlux 기반 SSE 스트리밍과 OS 스레드 최적화

### 1. Spring MVC + WebFlux의 스레드 병목 현상과 Virtual Threads
Tomcat 기반의 Spring MVC 환경에서 WebClient를 사용해 `Flux`를 반환하면, 외부 API(OpenAI) 호출은 Netty의 논블로킹 EventLoop에서 효율적으로 처리됩니다. 하지만 최종적으로 클라이언트에게 스트리밍 Chunk를 내려보낼 때(Servlet `OutputStream.write()`), 스프링 내부적으로 `SimpleAsyncTaskExecutor` 또는 톰캣 워커 스레드가 블로킹(Blocking)되며 대기하는 치명적인 병목이 발생합니다.
이를 해결하기 위해 WebFlux(Netty)로 전면 재작성하는 대신, **Java 21 가상 스레드(Virtual Threads)**를 활성화(`spring.threads.virtual.enabled: true`)했습니다. 가상 스레드는 블로킹 I/O 작업 시 OS 스레드를 점유하지 않고 양보(Unmount)하므로, 수천 개의 동시 스트리밍 요청도 적은 수의 OS 스레드만으로 거뜬히 처리하는 고효율 아키텍처를 완성했습니다.

### 2. ServerSentEvent 네이티브 디코딩과 엣지 케이스 방어
스트리밍 응답을 단순 `String`으로 받아서 `data: ` 접두어를 잘라내는 방식은 `[DONE]` 시그널 처리나 OpenAI의 정책 위반(`finish_reason: "content_filter"`)을 감지하기 어렵습니다.
* **해결책**: WebClient의 `bodyToFlux(object : ParameterizedTypeReference<ServerSentEvent<String>>() {})`를 사용하여 프레임워크 단에서 SSE 포맷을 네이티브하게 디코딩합니다.
* **효과**: 빈 이벤트 필터링(`isNullOrBlank`), 안전한 JSON DTO 매핑, 그리고 정책 위반 감지 시 `StreamEvent.Filtered` 상태로 변환하여 부분 응답과 함께 안내 메시지를 클라이언트로 안전하게 스트리밍할 수 있습니다.

### 3. 스트리밍 생명주기(Lifecycle) 기반 에러 핸들링
스트리밍 통신은 단발성 REST API와 달리 응답이 커밋(Commit)되는 시점을 기준으로 예외 처리 전략이 완전히 달라져야 합니다.
* **스트리밍 커밋 전 (Pre-commit)**: 클라이언트에게 HTTP 상태 코드를 내려주기 전(예: OpenAI 401/429 발생 시)이므로, 예외를 위로 던져 `GlobalExceptionHandler`에서 정상적인 4xx/5xx JSON 응답을 반환합니다.
* **스트리밍 커밋 후 (Post-commit)**: 이미 HTTP 200 OK가 클라이언트에게 전달된 후 스트림 도중 에러가 발생한 경우, 상태 코드를 바꿀 수 없습니다. 따라서 `onErrorResume` 연산자를 활용하여 **SSE 인밴드 이벤트(`event: error\ndata: ...`)** 형태로 클라이언트 브라우저에 에러 사실을 우아하게 알리고 스트림을 닫습니다.
