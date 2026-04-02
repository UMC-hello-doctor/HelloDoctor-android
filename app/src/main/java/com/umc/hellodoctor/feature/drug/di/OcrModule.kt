package com.umc.hellodoctor.feature.drug.di

import android.content.Context
import com.umc.hellodoctor.feature.drug.data.repository.OcrRepositoryImpl
import com.umc.hellodoctor.feature.drug.domain.repository.OcrRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OcrModule {
    @Provides
    @Singleton
    fun provideOcrRepository(
        @ApplicationContext context: Context,
    ): OcrRepository = OcrRepositoryImpl(context)
}
