package com.example.streamq.service

import com.example.streamq.domain.user.Provider
import com.example.streamq.domain.user.User
import com.example.streamq.domain.user.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.stereotype.Service

@Service
class UserService (
    private val userRepository: UserRepository
){
    @Transactional
    fun getOrRegisterUser(providerStr: String, attributes: Map<String, Any>): User {
        val providerId = attributes["sub"]?.toString()?.takeIf { it.isNotBlank() }
            ?: throw OAuth2AuthenticationException(OAuth2Error("invalid_request"), "구글 인증 응답에 필수 식별자(sub)가 없습니다.")
        val email = attributes["email"]?.toString()?.takeIf { it.isNotBlank() }
            ?: throw OAuth2AuthenticationException(OAuth2Error("invalid_request"), "구글 인증 응답에 필수 이메일 정보가 없습니다.")
        val name = attributes["name"]?.toString() ?: "Unknown"
        val picture = attributes["picture"]?.toString()
        val provider = Provider.valueOf(providerStr.uppercase())

        return userRepository.findByProviderAndProviderId(provider, providerId)
            ?.apply {
                this.name = name
                this.picture = picture
            }
            ?: userRepository.save(
                User(
                    email = email,
                    provider = provider,
                    providerId = providerId,
                    name = name,
                    picture = picture
                )
            )
    }
}
