package com.example.streamq.global.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import java.net.URLEncoder

@Component
class OAuth2FailureHandler(
    @Value("\${app.oauth2.authorized-redirect-uri}")
    private val redirectUri: String
) : SimpleUrlAuthenticationFailureHandler() {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val errorMessage = exception.message ?: "oauth2_login_failed"
        val targetUrl = "$redirectUri?error=${URLEncoder.encode(errorMessage, "UTF-8")}"
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
