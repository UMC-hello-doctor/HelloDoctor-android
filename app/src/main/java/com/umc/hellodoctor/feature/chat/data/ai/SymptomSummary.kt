package com.umc.hellodoctor.feature.chat.data.ai

import com.google.gson.annotations.SerializedName

/**
 * 증상 요약 응답 데이터 클래스
 */
data class SymptomSummaryResponse(
    @SerializedName("original")
    val original: OriginalLanguageData? = null,
    @SerializedName("korean")
    val korean: List<SummaryItem>? = null,
)

/**
 * 원본 언어 데이터
 */
data class OriginalLanguageData(
    @SerializedName("language")
    val language: String = "",
    @SerializedName("languageName")
    val languageName: String = "",
    @SerializedName("data")
    val data: List<SummaryItem>? = null,
)

/**
 * 요약 항목
 */
data class SummaryItem(
    @SerializedName("category")
    val category: String = "",
    @SerializedName("description")
    val description: String = "",
)
