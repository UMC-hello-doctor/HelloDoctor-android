package com.umc.hellodoctor.feature.chat.data.ai

import com.google.gson.annotations.SerializedName

// AI 응답 JSON 구조
data class DepartmentRecommendation(
    @SerializedName("department")
    val department: String, // "내과 - 소화기", "피부과" 등

    @SerializedName("departmentKo")
    val departmentKo: String, // "정형외과" (한글 이름, API 요청용)

    @SerializedName("departmentEn")
    val departmentEn: String, // "Gastroenterology", "Dermatology" 등

    @SerializedName("confidence")
    val confidence: Double, // 0.0 ~ 1.0 (확신도)

    @SerializedName("reason")
    val reason: String, // 추천 이유

    @SerializedName("nextQuestions")
    val nextQuestions: List<String>? = null, // PQRST 질문들

    @SerializedName("isEmergency")
    val isEmergency: Boolean = false, // Red Flag 감지

    @SerializedName("emergencyReason")
    val emergencyReason: String? = null
)
