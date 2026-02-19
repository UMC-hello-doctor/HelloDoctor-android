package com.umc.hellodoctor.feature.userinfo.data.remote.model

data class MyProfileResponse(
    val displayName: String,
    val birthDate: String,
    val gender: String,         // "MALE", "FEMALE"
    val bloodType: String,      // "A", "B", "O", "AB"
    val tags: List<String>      // UI에 칩 형태로 보여줄 요약 정보 (예: "항생제 알레르기")
)
