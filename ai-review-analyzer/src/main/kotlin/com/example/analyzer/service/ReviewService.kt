package com.example.analyzer.service

import com.example.analyzer.client.AiApiClient
import com.example.analyzer.domain.Review
import com.example.analyzer.repository.ReviewRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewService (
    private val reviewRepository: ReviewRepository,
    private val aiApiClient: AiApiClient
) {
    @Transactional
    fun analyzeReview(content: String): Review {
        // 1. 외부 API 통신 (예외 발생 시 밖으로 그대로 던져짐)
        val aiResponse = aiApiClient.requestAnalysis(content)
        
        // 2. 정상 처리 시에만 DB 저장 로직 수행
        val review = Review(
            content = content,
            sentiment = aiResponse.sentiment,
            summary = aiResponse.summary
        )
        return reviewRepository.save(review)
    }
}
