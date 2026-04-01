package com.umc.hellodoctor.feature.drug.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.umc.hellodoctor.feature.drug.data.model.MedicineSearchItem

class DrugConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromAlarmInfoList(value: List<AlarmInfo>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAlarmInfoList(value: String?): List<AlarmInfo>? {
        if (value == null) return null
        val listType = object : TypeToken<List<AlarmInfo>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromMedicineList(value: List<MedicineSearchItem>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toMedicineList(value: String?): List<MedicineSearchItem>? {
        if (value == null) return null
        val listType = object : TypeToken<List<MedicineSearchItem>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromIntakeType(value: IntakeType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toIntakeType(value: String?): IntakeType? {
        if (value == null) return null
        return IntakeType.valueOf(value)
    }
}
