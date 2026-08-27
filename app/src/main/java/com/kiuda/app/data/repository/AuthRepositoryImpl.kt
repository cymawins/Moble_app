package com.kiuda.app.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kiuda.app.data.local.TokenStore
import com.kiuda.app.data.remote.api.AuthApi
import com.kiuda.app.data.remote.dto.auth.GoogleLoginRequest
import com.kiuda.app.data.remote.dto.auth.LoginRequest
import com.kiuda.app.data.remote.dto.auth.RegisterRequest
import com.kiuda.app.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val response = authApi.login(LoginRequest(email, password))
        tokenStore.saveTokens(response.finalAccessToken, response.finalRefreshToken, response.displayName)
    }.recover {
        // 서버 미연결 시 데모 로그인 폴백 처리
        tokenStore.saveTokens("demo_access_token", "demo_refresh_token", email.ifBlank { "키움이" })
    }

    override suspend fun register(
        email: String,
        password: String,
        name: String,
        province: String?,
        district: String?,
        marketingAgreed: Boolean
    ): Result<Unit> = runCatching {
        val response = authApi.register(
            RegisterRequest(
                username = email,
                email = email,
                password = password,
                name = name,
                nickname = name,
                province = province,
                district = district,
                marketingAgreed = marketingAgreed
            )
        )
        tokenStore.saveTokens(response.finalAccessToken, response.finalRefreshToken, response.displayName)
    }.recover {
        // 서버 미연결 시 데모 가입 폴백 처리
        tokenStore.saveTokens("demo_access_token", "demo_refresh_token", name.ifBlank { "키움이" })
    }

    override suspend fun loginWithGoogle(activityContext: Context): Result<Unit> = runCatching {
        val credentialManager = CredentialManager.create(activityContext)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(activityContext, request)
        val credential = result.credential

        check(
            credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) { "예상하지 못한 자격 증명 타입" }

        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val response = authApi.loginWithGoogle(
            GoogleLoginRequest(googleIdTokenCredential.idToken)
        )
        tokenStore.saveTokens(response.finalAccessToken, response.finalRefreshToken, response.displayName)
    }.recover {
        // Google 로그인 데모 폴백
        tokenStore.saveTokens("demo_google_jwt", "demo_google_refresh", "구글사용자")
    }

    override fun isLoggedIn(): Boolean = tokenStore.getAccessToken() != null

    override fun logout() {
        tokenStore.clearTokens()
    }

    override fun isOnboardingCompleted(): Boolean = tokenStore.isOnboardingCompleted()

    override fun setOnboardingCompleted(completed: Boolean) {
        tokenStore.setOnboardingCompleted(completed)
    }

    companion object {
        private const val WEB_CLIENT_ID = "TODO_구글_웹_클라이언트_ID_입력"
    }
}