package com.example.streamq.service

import com.example.streamq.domain.user.Provider
import com.example.streamq.domain.user.User
import com.example.streamq.domain.user.UserRepository
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService (
    private val userRepository: UserRepository
){
    @Transactional
    fun getOrRegisterUser(providerStr: String, attributes: Map<String, Any>): User {
        val userInfo = extractOAuth2UserInfo(providerStr, attributes)

        return userRepository.findByProviderAndProviderId(userInfo.provider, userInfo.providerId)
            ?.apply {
                this.name = userInfo.name
                this.picture = userInfo.picture
            }
            ?: userRepository.save(
                User(
                    email = userInfo.email,
                    provider = userInfo.provider,
                    providerId = userInfo.providerId,
                    name = userInfo.name,
                    picture = userInfo.picture
                )
            )
    }

    private fun extractOAuth2UserInfo(providerStr: String, attributes: Map<String, Any>): OAuth2UserInfo {
        val providerId = attributes["sub"]?.toString()?.takeIf { it.isNotBlank() }
            ?: throw OAuth2AuthenticationException(OAuth2Error("invalid_request"), "구글 인증 응답에 필수 식별자(sub)가 없습니다.")
        
        val email = attributes["email"]?.toString()?.takeIf { it.isNotBlank() }
            ?: throw OAuth2AuthenticationException(OAuth2Error("invalid_request"), "구글 인증 응답에 필수 이메일 정보가 없습니다.")
            
        val name = attributes["name"]?.toString() ?: "Unknown"
        val picture = attributes["picture"]?.toString()
        
        val provider = try {
            Provider.valueOf(providerStr.uppercase())
        } catch (e: IllegalArgumentException) {
            throw OAuth2AuthenticationException(OAuth2Error("invalid_request"), "지원하지 않는 소셜 로그인 제공자입니다.")
        }

        return OAuth2UserInfo(providerId, email, name, picture, provider)
    }

    private data class OAuth2UserInfo(
        val providerId: String,
        val email: String,
        val name: String,
        val picture: String?,
        val provider: Provider
    )
}
