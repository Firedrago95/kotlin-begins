# 스파르타 코틀린 단기 속성 멘토링 로그북 (Kotlin Begins)

이 문서는 자바 스프링 전문가가 코틀린 스프링 고수로 거듭나기 위한 훈련 기록입니다.
주요 학습 키워드를 간략히 기록하고, 상세 내용은 블로그 포스팅에서 다룹니다.

---

## 📝 [1일차] 코틀린의 객체지향과 정적(Static) 메서드의 분리
*   **Top-level Function (최상위 함수):** 자바의 무의미한 유틸리티 클래스(`XxxUtils`)를 대체. 도메인 로직이 아닌 순수 유틸리티나 진입점(`main`)에만 사용.
*   **Companion Object (동반 객체):** 자바의 정적 팩토리 메서드(`static create()`) 등 도메인 객체와 강하게 결합된 정적 행위는 클래스 내부의 동반 객체로 캡슐화하여 객체지향적 응집도를 유지.

## 📝 [1일차] 코틀린의 엔티티 설계 (Data Class & Null Safety)
*   **`val` / `var`:** 참조 불변성(Reference Immutability) 명시. 영속화 시 리플렉션을 사용하는 JPA의 특성을 고려하더라도 애플리케이션 레벨의 불변성을 위해 `val`을 기본으로 사용.
*   **주 생성자 (Primary Constructor):** 필드 선언, 할당, Getter/Setter 보일러플레이트를 제거하고, 객체 생성 시점의 데이터 완전성을 강제.
*   **`data class`:** `toString()`, `equals()`, `hashCode()` 외에도 불변 객체의 일부 상태만 변경하여 복사하는 `copy()`, 구조 분해 할당(`componentN`) 기능 자동 생성.
*   **🚨 [주의] JPA Entity와 `data class`의 치명적 궁합:** 일반적인 DTO나 VO에서는 `data class`가 절대적인 표준이다. 하지만 **JPA Entity에서는 **N+1 문제**나 **`LazyInitializationException`**을 유발할 수 있다. 엔티티는 일반 `class`를 사용하고 식별자(ID) 기반으로 직접 `equals/hashCode`를 재정의하는 것이 안전하다.
