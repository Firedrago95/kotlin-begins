package com.example.streamq.service

import com.example.streamq.domain.user.Provider
import com.example.streamq.domain.user.User
import com.example.streamq.domain.user.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class UserService (
    private val userRepository: UserRepository
){
    @Transactional
    fun getOrRegisterUser(providerStr: String, attributes: Map<String, Any>): User {
        val providerId = attributes["sub"]?.toString() ?: ""
        val email = attributes["email"]?.toString() ?: ""
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
