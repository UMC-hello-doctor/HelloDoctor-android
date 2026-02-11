package com.umc.hellodoctor.feature.userinfo.presentation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.hellodoctor.feature.userinfo.data.remote.model.BloodType as ApiBloodType
import com.umc.hellodoctor.feature.userinfo.data.remote.model.GenderType as ApiGenderType
import com.umc.hellodoctor.feature.userinfo.data.remote.model.GenderType
import com.umc.hellodoctor.feature.userinfo.data.remote.model.AllergyType
import com.umc.hellodoctor.feature.userinfo.data.remote.model.ProfileCreateRequest
import com.umc.hellodoctor.feature.userinfo.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val repository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserInfoUiState())
    val uiState: StateFlow<UserInfoUiState> = _uiState

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState

    val isSaveEnabled: StateFlow<Boolean> =
        uiState.map { s -> s.name.isNotBlank() && s.gender != null && s.birthDate.isNotBlank() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun submitProfile(token: String) {
        val s = uiState.value

        val req = ProfileCreateRequest(
            displayName = s.name,
            gender = if (s.gender == Gender.FEMALE) ApiGenderType.FEMALE else ApiGenderType.MALE,
            birthDate = s.birthDate, // 이미 "YYYY-MM-DD" 형태로 들어오게 만들어둔 상태
            bloodType = when (s.bloodType) {
                BloodType.A -> ApiBloodType.A
                BloodType.B -> ApiBloodType.B
                BloodType.O -> ApiBloodType.O
                BloodType.AB -> ApiBloodType.AB
                BloodType.OTHER -> ApiBloodType.OTHER
                null -> ApiBloodType.OTHER
            },
            bloodTypeDetail = if (s.bloodType == BloodType.OTHER) s.bloodTypeOtherText.takeIf { it.isNotBlank() } else null,
            hasAllergy = (s.hasAllergy == YesNo.YES),
            allergyTypes = if (s.hasAllergy == YesNo.YES) {
                s.allergySelectedTags.map { tag ->
                    when (tag) {
                        AllergyTag.ANTIBIOTIC -> AllergyType.ANTIBIOTIC
                        AllergyTag.NSAID -> AllergyType.NSAID
                        AllergyTag.VACCINE -> AllergyType.VACCINE
                        AllergyTag.LOCAL_ANESTHETIC -> AllergyType.LOCAL_ANESTHETIC
                        AllergyTag.OTHER -> AllergyType.OTHER
                    }
                }
            } else emptyList(),
            takesMedication = (s.hasMeds == YesNo.YES),
            isPregnant = (s.isPregnantOrBreastfeeding == YesNo.YES)
        )

        viewModelScope.launch {
            _submitState.value = SubmitState.Loading
            runCatching {
                repository.createProfile(token, req)
            }.onSuccess {
                _submitState.value = SubmitState.Success
            }.onFailure { e ->
                _submitState.value = SubmitState.Error(e.message ?: "업로드 실패")
            }
        }
    }

    fun clearSubmitState() {
        _submitState.value = SubmitState.Idle
    }
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

    fun toggleAllergyTag(tag: AllergyTag) {
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

sealed interface SubmitState {
    data object Idle : SubmitState
    data object Loading : SubmitState
    data object Success : SubmitState
    data class Error(val message: String) : SubmitState
}
