package com.umc.hellodoctor.app.di

import com.umc.hellodoctor.feature.userinfo.data.api.UserInfoApi
import com.umc.hellodoctor.feature.userinfo.data.repository.UserInfoRepositoryImpl
import com.umc.hellodoctor.feature.userinfo.domain.repository.UserInfoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserInfoModule {

    @Provides
    @Singleton
    fun provideUserInfoRepository(
        userInfoApi: UserInfoApi
    ): UserInfoRepository = UserInfoRepositoryImpl(userInfoApi)
}

