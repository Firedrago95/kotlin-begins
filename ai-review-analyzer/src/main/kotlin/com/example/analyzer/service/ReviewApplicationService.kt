package com.example.analyzer.service

import com.example.analyzer.client.AiApiClient
import com.example.analyzer.presentation.dto.ReviewRequest
import com.example.analyzer.presentation.dto.ReviewResponse
import org.springframework.stereotype.Service

@Service
class ReviewApplicationService(
    private val aiApiClient: AiApiClient,
    private val reviewService: ReviewService
) {
    // 트랜잭션을 걸지 않습니다! (외부 API 호출 중 DB 커넥션 점유 방지)
    fun processReview(request: ReviewRequest): ReviewResponse {
        // 1. 외부 API 호출 (DB 트랜잭션 밖에서 실행됨)
        val aiResponse = aiApiClient.requestAnalysis(request.content)
        
        // 2. 분석 결과를 바탕으로 DB 저장 (이 메서드 내부에서만 트랜잭션 동작)
        val savedReview = reviewService.saveAnalyzedReview(
            content = request.content,
            sentiment = aiResponse.sentiment,
            summary = aiResponse.summary
        )

        // 3. Entity를 DTO로 변환하여 반환 (Controller 계층으로 전달)
        return ReviewResponse(
            id = savedReview.id ?: throw IllegalStateException("DB 저장 실패: ID가 생성되지 않았습니다."),
            sentiment = savedReview.sentiment ?: "UNKNOWN",
            summary = savedReview.summary ?: "요약 실패"
        )
    }
}
