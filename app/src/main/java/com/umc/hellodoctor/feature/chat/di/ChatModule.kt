package com.umc.hellodoctor.feature.chat.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.umc.hellodoctor.BuildConfig
import com.umc.hellodoctor.feature.chat.data.ai.AiRepository
import com.umc.hellodoctor.feature.chat.data.ai.AiService
import com.umc.hellodoctor.feature.chat.data.local.ChatDatabase
import com.umc.hellodoctor.feature.chat.data.local.ChatSessionDao
import com.umc.hellodoctor.feature.chat.data.repository.ChatRepositoryImpl
import com.umc.hellodoctor.feature.chat.domain.repository.ChatDataManager
import com.umc.hellodoctor.feature.chat.domain.repository.ChatRepository
import com.umc.hellodoctor.feature.chat.domain.repository.DepartmentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Chat 모듈 - Dependency Injection 설정
 */
@Module
@InstallIn(SingletonComponent::class)
object ChatModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()



    @Provides
    @Singleton
    @Named("AI_Retrofit")
    fun provideAiRetrofit(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAiService(@Named("AI_Retrofit") retrofit: Retrofit): AiService {
        return retrofit.create(AiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAiRepository(aiService: AiService, gson: Gson, @Named("AI_API_KEY") apiKey: String?): AiRepository {
        return AiRepository(aiService, gson, apiKey ?: "")
    }

    @Provides
    @Named("AI_API_KEY")
    fun provideApiKey(): String? {
        // BuildConfig.AI_API_KEY is populated from local.properties if available
        return BuildConfig.AI_API_KEY.ifBlank { null }
    }

    @Provides
    @Singleton
    fun provideDepartmentRepository(@ApplicationContext context: Context): DepartmentRepository {
        return DepartmentRepository(context)
    }

    @Provides
    @Singleton
    fun provideChatDataManager(aiRepository: AiRepository?): ChatDataManager {
        return ChatDataManager(aiRepository)
    }

    // ========== Room Database ==========

    @Provides
    @Singleton
    fun provideChatDatabase(@ApplicationContext context: Context): ChatDatabase {
        return Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            "chat_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideChatSessionDao(database: ChatDatabase): ChatSessionDao {
        return database.chatSessionDao()
    }

    @Provides
    @Singleton
    fun provideChatRepository(dao: ChatSessionDao): ChatRepository {
        return ChatRepositoryImpl(dao)
    }
}
