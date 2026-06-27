package com.example.analyzer.service

import com.example.analyzer.client.AiApiClient
import com.example.analyzer.domain.Review
import org.springframework.stereotype.Service

@Service
class ReviewApplicationService(
    private val aiApiClient: AiApiClient,
    private val reviewService: ReviewService
) {
    fun processReview(content: String): Review {
        // 1. 외부 API 호출 (DB 트랜잭션 밖)
        val aiResponse = aiApiClient.requestAnalysis(content)
        
        // 2. 분석 결과를 바탕으로 DB 저장
        return reviewService.saveAnalyzedReview(
            content = content,
            sentiment = aiResponse.sentiment,
            summary = aiResponse.summary
        )
    }
}
