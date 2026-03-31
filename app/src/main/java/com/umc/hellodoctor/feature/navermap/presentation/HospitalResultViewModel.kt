package com.umc.hellodoctor.feature.navermap.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.hellodoctor.feature.navermap.data.HospitalApi
import com.umc.hellodoctor.feature.navermap.data.HospitalItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HospitalResultViewModel
    @Inject
    constructor(
        private val hospitalApi: HospitalApi,
    ) : ViewModel() {
        private val _hospitalList = MutableLiveData<List<HospitalItem>>()
        val hospitalList: LiveData<List<HospitalItem>> = _hospitalList

        private val _uiState = MutableLiveData<ApiState>()
        val uiState: LiveData<ApiState> = _uiState

        @Suppress("TooGenericExceptionCaught")
        fun fetchNearbyHospitals(
            lat: Double,
            lng: Double,
            department: String = "내과",
        ) {
            viewModelScope.launch {
                _uiState.value = ApiState.Loading
                try {
                    Log.d("HospitalVM", "========== 병원 검색 API 요청 시작 ==========")
                    Log.d("HospitalVM", "요청 좌표: lat=$lat, lng=$lng")
                    Log.d("HospitalVM", "검색 진료과: $department")

                    val response = hospitalApi.getNearbyHospitals(lat, lng, department)

                    Log.d("HospitalVM", "========== 병원 검색 API 응답 ==========")
                    Log.d("HospitalVM", "응답 성공 여부: ${response.success}")
                    Log.d("HospitalVM", "응답 메시지: ${response.message}")
                    Log.d("HospitalVM", "병원 개수: ${response.result.size}")

                    if (response.success) {
                        val hospitals = response.result
                        hospitals.forEachIndexed { index, hospital ->
                            Log.d(
                                "HospitalVM",
                                "병원[$index]: 이름=${hospital.name}, " +
                                    "주소=${hospital.address}, 거리=${hospital.distance}m, " +
                                    "전화=${hospital.tel}, 영업시간=${hospital.businessHours}",
                            )
                        }

                        _hospitalList.value = hospitals.sortedBy { it.distance }
                        _uiState.value = ApiState.Success(hospitals.size)
                        Log.d("HospitalVM", "========== 병원 검색 성공 ==========")
                    } else {
                        _uiState.value = ApiState.Error(response.message)
                        Log.e("HospitalVM", "API 에러: ${response.message}")
                        Log.d("HospitalVM", "========== 병원 검색 실패 ==========")
                    }
                } catch (e: Exception) {
                    Log.e("HospitalVM", "Exception: ${e.message}", e)
                    Log.e("HospitalVM", "StackTrace: ${e.stackTraceToString()}")
                    _uiState.value = ApiState.Error("네트워크 오류: ${e.message}")
                    Log.d("HospitalVM", "========== 병원 검색 예외 발생 ==========")
                }
            }
        }
    }

// UI 상태
sealed class ApiState {
    object Loading : ApiState()

    data class Success(val count: Int) : ApiState()

    data class Error(val message: String) : ApiState()
}
