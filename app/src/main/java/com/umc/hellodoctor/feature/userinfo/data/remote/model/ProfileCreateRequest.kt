package com.umc.hellodoctor.feature.userinfo.data.remote.model
import com.umc.hellodoctor.feature.userinfo.data.remote.model.AllergyType

data class ProfileCreateRequest(
    val displayName: String,
    val gender: GenderType,
    val birthDate: String,          // "YYYY-MM-DD"
    val bloodType: BloodType,
    val bloodTypeDetail: String?,   // 기타일 때만 값, 아니면 null 권장
    val hasAllergy: Boolean,
    val allergyTypes: List<AllergyType>,
    val takesMedication: Boolean,
    val isPregnant: Boolean
)

enum class GenderType { FEMALE, MALE }
enum class BloodType { A, B, O, AB, OTHER }


