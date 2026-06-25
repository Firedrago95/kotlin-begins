package com.example.analyzer.domain

import jakarta.persistence.*

// TODO: 코틀린의 Data Class와 가변/불변(val/var), Null Safety(?), 기본값 지정 등을 활용하여 엔티티를 완성해라.
// 자바의 @Entity, @Id, @GeneratedValue 등은 동일하게 사용한다.

/*
[요구사항]
1. 테이블명: reviews
2. 필드:
   - id: Long (PK, Auto Increment), 초기엔 값이 없으므로 Null 허용 타입(?) 사용
   - content: String (리뷰 원본 내용, 생성 후 불변)
   - sentiment: String (감정 분석 결과, 분석 전엔 Null일 수 있음, 가변)
   - summary: String (요약 내용, 분석 전엔 Null일 수 있음, 가변)
*/

// 코드를 작성해라.
@Entity
@Table(name = "reviews")
class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val content: String,
    var sentiment: String? = null,
    var summary: String? = null
)
