package com.kiuda.app.data.remote.interceptor

import com.kiuda.app.data.local.TokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject


class AuthInterceptor @Inject constructor(
    //Hilt가 TokenStore를 자동으로 넣어줌
    private val tokenStore: TokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val accessToken = tokenStore.getAccessToken()

        val request = if (accessToken != null) {
            original.newBuilder()
                //저장된 토큰이 있으면 모든 API 요청 헤더에 자동으로 실어줌
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            original
            // 토큰이 없으면(로그인 전 상태) 원래 요청 그대로 보냄 — 로그인/회원가입 API는 토큰이 필요 없으니 문제 없음
        }
        return chain.proceed(request)
    }
}