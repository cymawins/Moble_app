package com.kiuda.app.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String? = null,
    val email: String,
    val password: String,
    val name: String,
    val nickname: String? = null,
    val province: String? = null,
    val district: String? = null,
    val marketingAgreed: Boolean = false
)