package com.umc.hellodoctor.feature.drug.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [DrugPlanEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(DrugConverters::class)
abstract class DrugDatabase : RoomDatabase() {
    abstract fun drugPlanDao(): DrugPlanDao
}
