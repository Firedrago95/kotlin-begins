package com.example.analyzer.presentation

import com.example.analyzer.presentation.dto.ReviewRequest
import com.example.analyzer.presentation.dto.ReviewResponse
import com.example.analyzer.service.ReviewApplicationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ReviewController (
    private val reviewApplicationService: ReviewApplicationService
){

    @PostMapping("/api/v1/reviews/analyze")
    fun analyzeReview(@RequestBody reviewRequest: ReviewRequest): ResponseEntity<ReviewResponse> {
        val responseDto = reviewApplicationService.processReview(reviewRequest)
        return ResponseEntity.ok().body(responseDto)
    }
}
