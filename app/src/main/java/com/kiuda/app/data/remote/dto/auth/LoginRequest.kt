package com.kiuda.app.data.remote.dto.auth

import kotlinx.serialization.Serializable

//kotlinx.serialization이 이 클래스를 JSON으로 변환할 수 있게 표시
@Serializable
data class LoginRequest (
    val email: String,
    val password: String
)

