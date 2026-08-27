package com.kiuda.app.di

import com.kiuda.app.data.repository.*
import com.kiuda.app.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository

    @Binds
    abstract fun bindDiagnosisRepository(impl: DiagnosisRepositoryImpl): DiagnosisRepository

    @Binds
    abstract fun bindNcpmsRepository(impl: NcpmsRepositoryImpl): NcpmsRepository
}