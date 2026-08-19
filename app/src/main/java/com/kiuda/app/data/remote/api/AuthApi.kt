package com.kiuda.app.data.remote.api

import com.kiuda.app.data.remote.dto.auth.AuthResponse
import com.kiuda.app.data.remote.dto.auth.GoogleLoginRequest
import com.kiuda.app.data.remote.dto.auth.LoginRequest
import com.kiuda.app.data.remote.dto.auth.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    //Retrofit이 이 인터페이스를 보고 실제 통신 코드를 자동 생성해줌 (우리는 선언만 하면 됨)

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): AuthResponse
}