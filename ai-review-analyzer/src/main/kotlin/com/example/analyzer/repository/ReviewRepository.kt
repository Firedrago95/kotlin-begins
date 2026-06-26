package com.example.analyzer.repository

import com.example.analyzer.domain.Review
import org.springframework.data.jpa.repository.JpaRepository

// TODO: ReviewRepository 인터페이스를 선언하고 JpaRepository를 상속(extends) 받아라.
// 힌트: 코틀린에서 클래스 상속과 인터페이스 구현은 모두 콜론(:) 하나로 통일된다.

interface ReviewRepository: JpaRepository<Review, Long>
