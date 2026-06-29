package com.example.analyzer.repository

import com.example.analyzer.IntegrationTestSupport
import com.example.analyzer.domain.Review
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

class ReviewRepositoryTest: IntegrationTestSupport() {

    @Autowired
    private lateinit var reviewRepository: ReviewRepository

    @Test
    fun `리뷰가 성공적으로 저장되어야 한다`() {
        // given
        val review = Review(content = "영화리뷰", summary = "러브스토리 리뷰", sentiment = "SAD")

        // when
        val savedReview = reviewRepository.save(review)

        // then
        assertThat(savedReview.id).isNotNull()
        assertThat(savedReview.summary).isEqualTo(review.summary)
        assertThat(savedReview.content).isEqualTo(review.content)
    }
}
