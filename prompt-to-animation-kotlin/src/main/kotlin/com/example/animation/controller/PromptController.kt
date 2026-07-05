package com.example.animation.controller

import com.example.animation.controller.dto.request.PromptRequest
import com.example.animation.controller.dto.response.PromptResponse
import com.example.animation.service.PromptApplicationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/prompt")
class PromptController (
    private val promptService: PromptApplicationService
){

    @PostMapping()
    fun generateCut(@RequestBody promptRequest: PromptRequest): ResponseEntity<PromptResponse> {
        val id = promptService.processPrompt(promptRequest)
        return ResponseEntity.ok().body(PromptResponse(id))
    }
}
