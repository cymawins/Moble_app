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


class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        // runCatching: Retrofit 호출이 실패(네트워크 에러, 4xx/5xx 등)하면 예외를 던지는 대신
        // Result.failure로 감싸줘서, 화면 쪽에서 try/catch 없이 안전하게 처리 가능
        val response = authApi.login(LoginRequest(email, password))
        tokenStore.saveTokens(response.accessToken, response.refreshToken)
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
            RegisterRequest(email, password, name, province, district, marketingAgreed)
        )
        //회원가입 성공 시 서버가 바로 JWT를 준다는 전제 (자동 로그인) - 백엔드 확정되면 재확인 필요
        tokenStore.saveTokens(response.accessToken, response.refreshToken)
    }

    override suspend fun loginWithGoogle(activityContext: Context): Result<Unit> = runCatching {
        val credentialManager = CredentialManager.create(activityContext)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) //기기에 등록된 구글 계정이 없어도 로그인 화면 띄움
            .setServerClientId(WEB_CLIENT_ID)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        //실제로 구글 계정 선택 UI를 띄우고, 사용자가 계정을 고르면 결과를 받아옴
        val result = credentialManager.getCredential(activityContext, request)
        val credential = result.credential

        check (
            credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ){"예상하지 못한 자격 증명 타입"}

        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val response = authApi.loginWithGoogle(
            GoogleLoginRequest(googleIdTokenCredential.idToken)
        )
        tokenStore.saveTokens(response.accessToken, response.refreshToken)
    }

    override fun isLoggedIn(): Boolean = tokenStore.getAccessToken() != null

    override fun logout() {
        tokenStore.clearTokens()
    }

    companion object {
        // ★ 반드시 교체 - Google Cloud Console > API 및 서비스 > 사용자 인증 정보에서
        // "OAuth 클라이언트 ID" (유형: 웹 애플리케이션)를 생성해서 나온 클라이언트 ID로 바꿔야
        // 실제 구글 로그인이 동작함. 지금은 컴파일만 되는 자리 표시자.
        private const val WEB_CLIENT_ID = "TODO_구글_웹_클라이언트_ID_입력"
    }

}