package com.kiuda.app.di

import com.kiuda.app.data.remote.api.AuthApi
import com.kiuda.app.data.remote.interceptor.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import javax.inject.Singleton

//Hilt에게 "이 클래스는 의존성 객체들을 만들어서 제공하는 곳"이라고 알려줌
@Module
//여기서 제공하는 객체들은 앱 실행 동안 딱 한번만 만들어져 앱 전역에서 재사용됨
@InstallIn (SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "http://10.0.2.2:3000/"
    // ★임시값★ — 10.0.2.2는 안드로이드 에뮬레이터에서 "내 컴퓨터(localhost)"를 가리키는 특수 주소.
    // 팀 백엔드(Node/Express) 개발 서버 주소가 나오면 반드시 이 값을 교체해야 함

    @Provides
    @Singleton
    fun  provideJson(): Json = Json {
        ignoreUnknownKeys = true  // 백엔드 응답에 우리가 모르는 필드가 섞여 있어도 에러 없이 무시
        coerceInputValues = true // 값이 없거나 형식이 살짝 달라도 최대한 관대하게 파싱
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 요청/응답 전체를 Logcat에 출력 (디버깅용 — 배포 전엔 낮춰야 함)
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) //JWT를 자동으로 실어주는 인터셉터
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit (okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            //Retrofit이 JSON ↔ Kotlin data class 변환에 kotlinx.serialization을 쓰도록 연결
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        //위에서 만든 Retrofit 인스턴스로, AuthApi 인터페이스의 실제 구현체를 자동 생성
        retrofit.create(AuthApi::class.java)

}