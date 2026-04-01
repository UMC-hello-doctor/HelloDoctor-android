package com.umc.hellodoctor.feature.chat.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.umc.hellodoctor.feature.chat.data.ai.SymptomSummaryResponse

/**
 * Room TypeConverter - 복잡한 타입 변환
 */
class Converters {
    private val gson = Gson()

    /**
     * List<String> -> JSON String
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return gson.toJson(value)
    }

    /**
     * JSON String -> List<String>
     */
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    /**
     * SymptomSummaryResponse -> JSON String
     */
    @TypeConverter
    fun fromSymptomSummaryResponse(value: SymptomSummaryResponse?): String? {
        return gson.toJson(value)
    }

    /**
     * JSON String -> SymptomSummaryResponse
     */
    @TypeConverter
    fun toSymptomSummaryResponse(value: String?): SymptomSummaryResponse? {
        if (value == null) return null
        return gson.fromJson(value, SymptomSummaryResponse::class.java)
    }
}
