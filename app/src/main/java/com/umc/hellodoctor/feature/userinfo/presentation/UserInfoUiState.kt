package com.umc.hellodoctor.feature.userinfo.presentation

data class UserInfoUiState(
    val name: String = "",
    val gender: Gender? = null,
    val birthDate: String = "",

    val bloodType: BloodType? = null,
    val isBloodOtherVisible: Boolean = false,
    val bloodTypeOtherText: String = "",

    val hasAllergy: YesNo? = null,
    val isAllergyDetailVisible: Boolean = false,
    val allergySelectedTags: Set<AllergyTag> = emptySet(),

    val hasMeds: YesNo? = null,
    val isPregnantOrBreastfeeding: YesNo? = null
)

enum class Gender { FEMALE, MALE }

enum class BloodType { A, B, O, AB, OTHER }

enum class YesNo { YES, NO }

enum class AllergyTag(val label: String) {
    ANTIBIOTIC("항생제"),
    NSAID("소염진통제"),
    VACCINE("백신 성분"),
    LOCAL_ANESTHETIC("국소 마취제"),
    OTHER("기타")
}
