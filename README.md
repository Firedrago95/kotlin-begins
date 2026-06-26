# 코틀린 학습

## 📝 [1일차] 객체지향과 엔티티 설계
*   **Top-level Function:** 유틸/진입점 용도. 자바의 무의미한 `Utils` 클래스 대체.
*   **Companion Object:** 도메인 결합 정적 메서드(`create()`) 캡슐화.
*   **`val` / `var`:** 기본은 무조건 `val`(참조 불변성). 참조가 아예 바뀔 때만 `var`.
*   **주 생성자(Primary Constructor):** 필드 선언, 할당, Getter/Setter를 1줄로 압축. 데이터 완전성 보장.
*   **Null Safety (`?`):** 컴파일 타임에 NPE 원천 차단.
*   **JPA Entity vs `data class`:** 일반 객체는 `data class`가 표준이나, JPA 엔티티에선 자동 생성된 `equals/toString`이 지연 로딩(N+1)을 터뜨릴 수 있어 일반 `class` 사용 권장.

## 📝 [2일차] 상속과 인터페이스
*   **상속/구현 통일 (`:`):** 자바의 `extends`, `implements`를 `:` 하나로 통합.
*   **생성자 괄호 `()`:** 부모가 클래스면 생성자 호출(`: Base()`), 인터페이스면 없음(`: Base`). 시각적 구분 명확.
*   **`override` 강제:** 재정의 시 `override` 키워드 강제. 미부착 시 컴파일 에러로 규약 준수 명확화.
*   **단일 표현식 함수 (`=`):** 함수가 단일 결과값으로 귀결될 때 `{}`와 `return`을 생략하고 `=`로 직관적으로 표현.
*   **타입 추론 (Type Inference):** 컴파일러가 반환값을 보고 타입을 완벽히 유추할 수 있을 때, 함수의 반환 타입(`: Type`) 선언을 생략 가능.
*   **우아한 에러 처리 (`runCatching` & `Result`):** 자바의 `try-catch`로 예외를 던지는(Throw) 흐름 제어 대신, 에러를 값(Value)으로 취급하여 `Result` 객체(Success/Failure)에 담아 반환하는 함수형 에러 처리 방식.

## 📝 [2일차 추가] 외부 API 통신 (RestClient와 동기/비동기)
*   **`RestClient` vs `WebClient`:** 비동기 WebFlux 생태계의 `WebClient` 대신, MVC 환경에서는 동기식이면서 체이닝 문법을 제공하는 `RestClient`가 현대적 표준.
*   **가상 스레드 (Virtual Threads)와의 조합:** 자바 21+ 환경에서 가상 스레드 설정을 켜면, `RestClient`의 블로킹 코드가 논블로킹처럼 동작(스케일링)하므로 복잡한 비동기 코드 없이도 높은 성능을 낸다.
*   **`CompletableFuture` vs 코루틴 (`Coroutine`):** 코틀린에서는 자바의 콜백이나 `CompletableFuture` 체이닝 대신, `suspend` 함수(코루틴)를 써서 비동기 코드를 동기 코드처럼 읽기 쉽게 작성한다.
