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
class HospitalResultViewModel @Inject constructor(
    private val hospitalApi: HospitalApi
) : ViewModel() {

    private val _hospitalList = MutableLiveData<List<HospitalItem>>()
    val hospitalList: LiveData<List<HospitalItem>> = _hospitalList

    private val _uiState = MutableLiveData<ApiState>()
    val uiState: LiveData<ApiState> = _uiState

    fun fetchNearbyHospitals(lat: Double, lng: Double, department: String = "내과") {
        viewModelScope.launch {
            _uiState.value = ApiState.Loading
            try {
                Log.d("HospitalVM", "API calling...")
                Log.d("HospitalVM", "fetchNearbyHospitals: $lat, $lng, $department")
                val response = hospitalApi.getNearbyHospitals(lat, lng, department)

                Log.d("HospitalVM", "API response: success=${response.success}, size=${response.result.size}")

                if (response.success) {
                    val hospitals = response.result
                    _hospitalList.value = hospitals.sortedBy { it.distance }
                    _uiState.value = ApiState.Success(hospitals.size)
                    Log.d("HospitalVM", "Success: ${hospitals.size} hospitals")
                } else {
                    _uiState.value = ApiState.Error(response.message)
                    Log.d("HospitalVM", "API error: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("HospitalVM", "Exception: ${e.message}", e)
                _uiState.value = ApiState.Error("네트워크 오류: ${e.message}")
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
