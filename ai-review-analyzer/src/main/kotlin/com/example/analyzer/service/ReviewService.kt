package com.example.analyzer.service

import com.example.analyzer.domain.Review
import com.example.analyzer.repository.ReviewRepository
import org.springframework.stereotype.Service

@Service
class ReviewService (
    val reviewRepository: ReviewRepository
) {
    fun analyzeReview(content: String) = runCatching {
        val review = Review(content = content)
        reviewRepository.save(review)
    }
}
