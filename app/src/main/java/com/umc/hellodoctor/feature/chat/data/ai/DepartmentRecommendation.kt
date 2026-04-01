package com.umc.hellodoctor.feature.chat.data.ai

import com.google.gson.annotations.SerializedName

// AI 응답 JSON 구조
// department: "내과 - 소화기", "피부과" 등
// departmentKo: "정형외과" (한글 이름, API 요청용)
// departmentEn: "Gastroenterology", "Dermatology" 등
// confidence: 0.0 ~ 1.0 (확신도)
// nextQuestions: PQRST 질문들
// isEmergency: Red Flag 감지
data class DepartmentRecommendation(
    @SerializedName("department")
    val department: String,
    @SerializedName("departmentKo")
    val departmentKo: String,
    @SerializedName("departmentEn")
    val departmentEn: String,
    @SerializedName("confidence")
    val confidence: Double,
    @SerializedName("reason")
    val reason: String,
    @SerializedName("nextQuestions")
    val nextQuestions: List<String>? = null,
    @SerializedName("isEmergency")
    val isEmergency: Boolean = false,
    @SerializedName("emergencyReason")
    val emergencyReason: String? = null,
)
