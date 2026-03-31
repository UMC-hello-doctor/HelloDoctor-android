package com.umc.hellodoctor.feature.userinfo.data.model

data class ProfileCreateRequest(
    val displayName: String,
    val gender: GenderType,
    // "YYYY-MM-DD"
    val birthDate: String,
    val bloodType: BloodType,
    // 기타일 때만 값, 아니면 null 권장
    val bloodTypeDetail: String?,
    val hasAllergy: Boolean,
    val allergyTypes: List<AllergyType>,
    val takesMedication: Boolean,
    val isPregnant: Boolean,
)

enum class GenderType { FEMALE, MALE }

enum class BloodType { A, B, O, AB, OTHER }
