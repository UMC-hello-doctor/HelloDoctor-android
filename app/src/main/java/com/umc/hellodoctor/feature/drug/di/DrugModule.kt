package com.umc.hellodoctor.feature.drug.di

import android.content.Context
import androidx.room.Room
import com.umc.hellodoctor.feature.drug.data.database.DrugDatabase
import com.umc.hellodoctor.feature.drug.data.database.DrugPlanDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DrugModule {
    @Provides
    @Singleton
    fun provideDrugDatabase(
        @ApplicationContext context: Context,
    ): DrugDatabase {
        return Room.databaseBuilder(
            context,
            DrugDatabase::class.java,
            "drug_database",
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDrugPlanDao(database: DrugDatabase): DrugPlanDao {
        return database.drugPlanDao()
    }
}
