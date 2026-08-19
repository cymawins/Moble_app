package com.kiuda.app.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse (
    //로그인, 회원가입, 구글로그인 등 3개 API가 전부 이 응답 형태를 공유 (성공 시 바로 JWT 발급)
    val accessToken: String,
    val refreshToken: String,
    val userId: Long
)