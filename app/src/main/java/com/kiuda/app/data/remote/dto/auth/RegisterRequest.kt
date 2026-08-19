package com.kiuda.app.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest (
    val email: String,
    val password: String,
    val name: String,
    val province: String? = null, //회원가입 2단계 지역 선택 - User.province
    val district: String? = null, //User.district
    val marketingAgreed: Boolean = false //User.marketing_agreed
)