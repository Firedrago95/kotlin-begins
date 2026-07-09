package com.example.streamq.global.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2SuccessHandler(
    @Value("\${app.oauth2.authorized-redirect-uri}")
    private val redirectUri: String,
    private val jwtTokenProvider: JwtTokenProvider
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication?
    ) {
        val customOAuth2User = authentication?.principal as CustomOAuth2User
        val userId = customOAuth2User.user.id.toString()
        val role = customOAuth2User.user.role.name

        val accessToken = jwtTokenProvider.generateToken(userId, role)
        val targetUrl = "$redirectUri?token=$accessToken"

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
