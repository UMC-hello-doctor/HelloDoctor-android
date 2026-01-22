package com.umc.hellodoctor.feature.userinfo.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
enum class Gender { FEMALE, MALE }
enum class BloodType { A, B, O, AB, OTHER }
enum class YesNo { YES, NO }

data class UserInfoUiState(
    val name: String = "",
    val gender: Gender? = null,
    val birthDate: String = "",
    val bloodType: BloodType? = null,
    val bloodTypeOtherText: String = "",
    val isBloodOtherVisible: Boolean = false,

    val hasAllergy: YesNo? = null,
    val isAllergyDetailVisible: Boolean = false,
    val allergySelectedTags: Set<String> = emptySet(),

    val hasMeds: YesNo? = null,
    val isPregnantOrBreastfeeding: YesNo? = null
)

class UserInfoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UserInfoUiState())
    val uiState: StateFlow<UserInfoUiState> = _uiState

    // 저장 버튼 활성화 여부 (유효성 검사 결과)
    val isSaveEnabled: StateFlow<Boolean> =
        uiState
            .map { state ->
                state.name.isNotBlank() &&
                        state.gender != null &&
                        state.birthDate.isNotBlank()
            }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                false
            )

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onGenderSelected(gender: Gender) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun onBirthDateSelected(date: String) {
        _uiState.update { it.copy(birthDate = date) }
    }

    fun onBloodTypeSelected(type: BloodType) {
        _uiState.update {
            it.copy(
                bloodType = type,
                isBloodOtherVisible = (type == BloodType.OTHER),
                bloodTypeOtherText = if (type == BloodType.OTHER) it.bloodTypeOtherText else ""
            )
        }
    }

    fun onBloodOtherTextChanged(text: String) {
        _uiState.update { it.copy(bloodTypeOtherText = text) }
    }

    fun onAllergySelected(answer: YesNo) {
        _uiState.update {
            it.copy(
                hasAllergy = answer,
                isAllergyDetailVisible = (answer == YesNo.YES),
                allergySelectedTags = if (answer == YesNo.YES) it.allergySelectedTags else emptySet()
            )
        }
    }

    fun toggleAllergyTag(tag: String) {
        _uiState.update { state ->
            val newSet =
                if (state.allergySelectedTags.contains(tag)) state.allergySelectedTags - tag
                else state.allergySelectedTags + tag

            state.copy(allergySelectedTags = newSet)
        }
    }

    fun onMedsSelected(answer: YesNo) {
        _uiState.update { it.copy(hasMeds = answer) }
    }

    fun onPregSelected(answer: YesNo) {
        _uiState.update { it.copy(isPregnantOrBreastfeeding = answer) }
    }
}
