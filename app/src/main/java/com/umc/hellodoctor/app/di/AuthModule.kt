package com.umc.hellodoctor.app.di

import com.umc.hellodoctor.feature.auth.data.repository.AuthApi
import com.umc.hellodoctor.feature.auth.domain.repository.AuthRepository
import com.umc.hellodoctor.feature.auth.domain.repository.AuthRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi
    ): AuthRepository = AuthRepositoryImpl(authApi)
}