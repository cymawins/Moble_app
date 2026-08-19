package com.kiuda.app.di

import com.kiuda.app.data.repository.AuthRepositoryImpl
import com.kiuda.app.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn (SingletonComponent::class)
abstract class RepositoryModule {
    //@Provides처럼 직접 객체를 만드는 게 아니라, "AuthRepository 인터페이스가 필요하면
    //AuthRepositoryImpl을 대신 줘라"라고 Hilt에게 연결만 해주는 방식 (더 가볍고 관례적)
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}