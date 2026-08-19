package com.kiuda.app.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

//Hilt에게 "앱 전체에서 이 클래스는 딱 하나의 인스턴스만 만들어라"고 지시. 토큰 저장소가 여러 개면 안되니 필수
@Singleton
class TokenStore @Inject constructor(
   //암호화에 쓸 키를 안전하게 생성/관리하는 역할
   @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "kiuda_auth_prefs", //저장 파일 이름
        masterKey,
        //key(이름) 암호화
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        //value(값) 암호화
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun clearTokens(){
        //로그아웃시 호출 - 저장된 토큰 전부 삭제
        prefs.edit().clear().apply()
    }
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}