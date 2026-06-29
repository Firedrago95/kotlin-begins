package com.example.analyzer.service

import com.example.analyzer.client.AiApiClient
import com.example.analyzer.client.AiResponse
import com.example.analyzer.domain.Review
import com.example.analyzer.presentation.dto.ReviewRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.bytebuddy.matcher.ElementMatchers.returns
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ReviewApplicationServiceTest {

    private val aiApiClient: AiApiClient = mockk()
    private val reviewService: ReviewService = mockk()

    private val reviewApplicationService = ReviewApplicationService(aiApiClient, reviewService)

    @Test
    @DisplayName("리뷰 분석 요청이 성공하면 변환된 DTO를 반환한다")
    fun processReviewSuccess() {
        // given
        val request = ReviewRequest(content = "최고의 영화입니다")
        
        every { aiApiClient.requestAnalysis(any()) } returns AiResponse(sentiment = "JOY", summary = "재밌다")

        val id = 1L
        val sentiment = "JOY"
        val content = "최고의 영화입니다"
        val summary = "재밌다"
        
        every { 
            reviewService.saveAnalyzedReview(any(), any(), any()) 
        } returns Review(id = id, sentiment = sentiment, content = content, summary = summary)

        // when
        val reviewResponse = reviewApplicationService.processReview(request)

        // then
        assertThat(reviewResponse.sentiment).isEqualTo(sentiment)
        verify(exactly = 1) { aiApiClient.requestAnalysis(any()) }
    }
}
