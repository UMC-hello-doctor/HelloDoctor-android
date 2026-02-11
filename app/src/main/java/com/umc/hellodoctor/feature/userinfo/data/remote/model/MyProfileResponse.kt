package com.umc.hellodoctor.feature.userinfo.data.remote.model

data class MyProfileResponse(
    val displayName: String,
    val gender: GenderType,
    val birthDate: String,
    val bloodType: BloodType,
    val bloodTypeDetail: String?,
    val hasAllergy: Boolean,
    val allergyTypes: List<AllergyType>,
    val takesMedication: Boolean,
    val isPregnant: Boolean
)
