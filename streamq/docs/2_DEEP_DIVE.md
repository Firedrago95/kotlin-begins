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
