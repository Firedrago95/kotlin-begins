package com.example.streamq.global.security

import com.example.streamq.domain.user.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

class CustomOAuth2User(
    val user: User,
    private val attributes: Map<String, Any>
) : OAuth2User {
    override fun getAttributes(): MutableMap<String, Any> = attributes.toMutableMap()

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        mutableListOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))

    override fun getName(): String = user.id.toString()
}
