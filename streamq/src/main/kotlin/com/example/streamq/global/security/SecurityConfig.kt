package com.example.streamq.global.security

import com.example.streamq.global.exception.ErrorCode
import com.example.streamq.global.exception.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig (
    private val jwtAuthFilter: JwtAuthFilter,
    private val objectMapper: ObjectMapper
){
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { cors ->
                cors.configurationSource {
                    val configuration = org.springframework.web.cors.CorsConfiguration()
                    configuration.allowedOriginPatterns = listOf("*") // TODO: 추후 프론트 도메인 확정 시 변경
                    configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                    configuration.allowedHeaders = listOf("*")
                    configuration.allowCredentials = true
                    configuration
                }
            }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/v1/auth/login", "/api/v1/auth/signup").permitAll()
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint { _, response, _ ->
                    FilterErrorResponseWriter.write(
                        response = response,
                        status = HttpServletResponse.SC_UNAUTHORIZED,
                        code = ErrorCode.UNAUTHORIZED_USER.code,
                        message = ErrorCode.UNAUTHORIZED_USER.message,
                        objectMapper = objectMapper
                    )
                }
                ex.accessDeniedHandler { _, response, _ ->
                    FilterErrorResponseWriter.write(
                        response = response,
                        status = HttpServletResponse.SC_FORBIDDEN,
                        code = ErrorCode.INVALID_INPUT_VALUE.code,
                        message = "접근 권한이 없습니다.",
                        objectMapper = objectMapper
                    )
                }
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
