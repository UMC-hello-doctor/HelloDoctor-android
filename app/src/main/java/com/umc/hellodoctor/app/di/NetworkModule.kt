package com.umc.hellodoctor.app.di

import com.umc.hellodoctor.BuildConfig
import com.umc.hellodoctor.feature.auth.data.repository.AuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(
        retrofit: Retrofit
    ): AuthApi = retrofit.create(AuthApi::class.java)

}