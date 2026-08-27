package com.kiuda.app.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val jwt: String? = null,
    val token: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val userId: Long? = null,
    val username: String? = null,
    val name: String? = null,
    val nickname: String? = null,
    val role: String? = null,
    val message: String? = null
) {
    val finalAccessToken: String
        get() = jwt ?: token ?: accessToken ?: "demo_access_token"

    val finalRefreshToken: String
        get() = refreshToken ?: "demo_refresh_token"

    val displayName: String
        get() = nickname ?: name ?: username ?: "키움이"
}