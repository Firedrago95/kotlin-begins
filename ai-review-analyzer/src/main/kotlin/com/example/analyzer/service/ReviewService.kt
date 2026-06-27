package com.example.analyzer.service

import com.example.analyzer.domain.Review
import com.example.analyzer.repository.ReviewRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewService (
    private val reviewRepository: ReviewRepository
) {
    @Transactional
    fun saveAnalyzedReview(content: String, sentiment: String, summary: String): Review {
        val review = Review(
            content = content,
            sentiment = sentiment,
            summary = summary
        )
        return reviewRepository.save(review)
    }
}
