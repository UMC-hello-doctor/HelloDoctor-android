package com.umc.hellodoctor.feature.userinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.hellodoctor.feature.userinfo.data.model.AllergyType
import com.umc.hellodoctor.feature.userinfo.data.model.BloodType
import com.umc.hellodoctor.feature.userinfo.data.model.GenderType
import com.umc.hellodoctor.feature.userinfo.data.model.MyProfileResponse
import com.umc.hellodoctor.feature.userinfo.data.model.ProfileCreateRequest
import com.umc.hellodoctor.feature.userinfo.domain.repository.UserInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserInfoUiState(
    // 기본정보
    val name: String = "",
    val gender: GenderType? = null,
    val birthDate: String? = null,
    val bloodType: BloodType? = null,
    val bloodTypeOther: String = "",
    // 건강상태
    val allergy: Boolean? = null,
    val allergyDetails: List<AllergyType> = emptyList(),
    val medication: Boolean? = null,
    val pregnancy: Boolean? = null,
    // API 상태
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    // 프로필 조회 데이터
    val profileData: MyProfileResponse? = null,
)

@HiltViewModel
class UserInfoViewModel
    @Inject
    constructor(
        private val repository: UserInfoRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(UserInfoUiState())
        val uiState: StateFlow<UserInfoUiState> = _uiState.asStateFlow()

        // 이름 설정
        fun setName(name: String) {
            _uiState.update { it.copy(name = name) }
        }

        // 성별 설정
        fun setGender(gender: GenderType) {
            _uiState.update { it.copy(gender = gender) }
        }

        // 생년월일 설정
        fun setBirthDate(date: String) {
            _uiState.update { it.copy(birthDate = date) }
        }

        // 혈액형 설정
        fun setBloodType(type: BloodType?) {
            _uiState.update { it.copy(bloodType = type) }
        }

        // 혈액형 직접 입력
        fun setBloodTypeOther(type: String) {
            _uiState.update { it.copy(bloodTypeOther = type) }
        }

        // 알레르기 여부 설정
        fun setAllergy(hasAllergy: Boolean) {
            _uiState.update {
                if (hasAllergy) {
                    it.copy(allergy = true)
                } else {
                    // 알레르기 없음 선택 시 상세내용 초기화
                    it.copy(allergy = false, allergyDetails = emptyList())
                }
            }
        }

        // 알레르기 상세 항목 토글
        fun toggleAllergyDetail(detail: AllergyType) {
            _uiState.update { state ->
                val newDetails = state.allergyDetails.toMutableList()
                if (newDetails.contains(detail)) {
                    newDetails.remove(detail)
                } else {
                    newDetails.add(detail)
                }
                state.copy(allergyDetails = newDetails)
            }
        }

        // 처방약/영양제 복용 여부
        fun setMedication(hasMedication: Boolean) {
            _uiState.update { it.copy(medication = hasMedication) }
        }

        // 임신/수유 여부
        fun setPregnancy(isPregnant: Boolean) {
            _uiState.update { it.copy(pregnancy = isPregnant) }
        }

        // 프로필 생성 API 호출
        fun createProfile() {
            val state = _uiState.value

            // 검증: 필수 항목 체크
            if (state.name.isBlank() || state.gender == null || state.birthDate == null || state.bloodType == null) {
                _uiState.update { it.copy(errorMessage = "필수 항목을 모두 입력해주세요") }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val request =
                    ProfileCreateRequest(
                        displayName = state.name,
                        gender = state.gender,
                        birthDate = state.birthDate,
                        bloodType = state.bloodType,
                        bloodTypeDetail = if (state.bloodType == BloodType.OTHER) state.bloodTypeOther else null,
                        hasAllergy = state.allergy ?: false,
                        allergyTypes = if (state.allergy == true) state.allergyDetails else emptyList(),
                        takesMedication = state.medication ?: false,
                        isPregnant = state.pregnancy ?: false,
                    )

                repository.createProfile(request)
                    .onSuccess { _ ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true,
                                errorMessage = null,
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = false,
                                errorMessage = error.message ?: "프로필 생성에 실패했습니다",
                            )
                        }
                    }
            }
        }

        // 프로필 조회 API 호출
        fun getMyProfile(forceRefresh: Boolean = false) {
            // 캐시된 데이터가 있고 강제 새로고침이 아니면 API 호출하지 않음
            val cached = _uiState.value.profileData
            if (cached != null && !forceRefresh) {
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                repository.getMyProfile()
                    .onSuccess { profileResponse ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                profileData = profileResponse,
                                errorMessage = null,
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "프로필 조회에 실패했습니다",
                            )
                        }
                    }
            }
        }
    }
