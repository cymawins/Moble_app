package com.kiuda.app.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class GoogleLoginRequest (
    val idToken: String //Credential Manager로 받은 구글 ID Token - 백엔드가 이걸로 서명 검증
)