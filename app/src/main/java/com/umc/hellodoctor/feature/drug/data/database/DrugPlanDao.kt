package com.umc.hellodoctor.feature.drug.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DrugPlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: DrugPlanEntity)

    @Update
    suspend fun updatePlan(plan: DrugPlanEntity)

    @Query("SELECT * FROM drug_plans WHERE id = :planId")
    suspend fun getPlanById(planId: String): DrugPlanEntity?

    @Query("SELECT * FROM drug_plans ORDER BY startDateMillis DESC")
    suspend fun getAllPlans(): List<DrugPlanEntity>

    @Query("DELETE FROM drug_plans WHERE id = :planId")
    suspend fun deletePlanById(planId: String)

    @Query("DELETE FROM drug_plans")
    suspend fun deleteAllPlans()
}

