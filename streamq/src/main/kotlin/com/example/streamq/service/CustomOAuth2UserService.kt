package com.example.streamq.service

import com.example.streamq.global.security.CustomOAuth2User
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService (
    private val userService: UserService
) : DefaultOAuth2UserService() {
    private val log  = LoggerFactory.getLogger(CustomOAuth2UserService::class.java)

    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val attributes = oAuth2User.attributes
        val providerStr = userRequest?.clientRegistration?.registrationId ?: "google"

        val user = userService.getOrRegisterUser(providerStr, attributes)

        return CustomOAuth2User(user, attributes)
    }
}
