package com.example.analyzer.presentation

import com.example.analyzer.presentation.dto.ReviewRequest
import com.example.analyzer.presentation.dto.ReviewResponse
import com.example.analyzer.service.ReviewApplicationService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayNameGeneration
import org.junit.jupiter.api.DisplayNameGenerator
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ReviewController::class)
@AutoConfigureRestDocs
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
class ReviewControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var reviewApplicationService: ReviewApplicationService

    @Test
    fun `리뷰 분석 요청 시 성공적으로 결과를 반환한다`() {
        // given
        val request = ReviewRequest(content = "이 영화 정말 재밌어요!")
        val response = ReviewResponse(id = 1L, sentiment = "JOY", summary = "긍정적인 리뷰")

        every { reviewApplicationService.processReview(any()) } returns response

        // when & then
        mockMvc.perform(
            post("/api/v1/reviews/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.sentiment").value("JOY"))
            .andExpect(jsonPath("$.summary").value("긍정적인 리뷰"))
            .andDo(
                document(
                    "review-analyze",
                    requestFields(
                        fieldWithPath("content").description("리뷰 내용 본문")
                    ),
                    responseFields(
                        fieldWithPath("id").description("저장된 리뷰 ID"),
                        fieldWithPath("sentiment").description("AI가 분석한 감정 상태"),
                        fieldWithPath("summary").description("리뷰 핵심 요약")
                    )
                )
            )

        val requestSlot = slot<ReviewRequest>()
        verify(exactly = 1) { reviewApplicationService.processReview(capture(requestSlot)) }
        assertThat(requestSlot.captured.content).isEqualTo("이 영화 정말 재밌어요!")
    }
}
