package com.kiuda.app.domain.repository

import android.content.Context

interface AuthRepository {
    //domain 레이어는 "무엇을 할 수 있는지"만 정의 — Retrofit/Credential Manager 같은 구체적 구현은 모름
    suspend fun login(email: String, password: String): Result<Unit>

    suspend fun register (
        email: String,
        password: String,
        name: String,
        province: String?,
        district: String?,
        marketingAgreed: Boolean

    ): Result<Unit>

    //Context가 필요한 이유: Credential Manager가 로그인 계정 선택시 UI를 띄워야 하기 때문
    //화면(Activity)과 연결된 Context가 있어야 함. 나중에 로그인 화면에서 이 함수를 호출할 때 넘겨주면 됨
    suspend fun loginWithGoogle(activityContext: Context): Result<Unit>

    fun isLoggedIn(): Boolean
    fun logout()

    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean)
}