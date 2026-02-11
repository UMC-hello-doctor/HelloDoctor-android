package com.umc.hellodoctor.app.di

import com.umc.hellodoctor.feature.userinfo.data.remote.UserProfileApi
import com.umc.hellodoctor.feature.userinfo.domain.repository.UserProfileRepository
import com.umc.hellodoctor.feature.userinfo.domain.repository.UserProfileRepositoryImpl
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
    fun provideUserProfileRepository(
        api: UserProfileApi
    ): UserProfileRepository = UserProfileRepositoryImpl(api)
}
