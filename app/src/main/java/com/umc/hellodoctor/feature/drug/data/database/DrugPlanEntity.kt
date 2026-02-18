package com.umc.hellodoctor.feature.drug.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.umc.hellodoctor.feature.drug.data.model.MedicineSearchItem

@Entity(tableName = "drug_plans")
@TypeConverters(DrugConverters::class)
data class DrugPlanEntity(
    @PrimaryKey
    val id: String,
    val pharmacyName: String,
    val intakeType: IntakeType,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val alarms: List<AlarmInfo>,
    val medicines: List<MedicineSearchItem>
)

