package com.umc.hellodoctor.feature.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.hellodoctor.core.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LocationMapUiState(
    val isLoadingLocation: Boolean = false,
    val currentLat: Double? = null,
    val currentLng: Double? = null,
    val cameraLat: Double? = null,
    val cameraLng: Double? = null,
    val locationError: String? = null
)
/**
 * 지도 화면에서 위치 상태를 관리하는 ViewModel.
 *
 * - 현재 위치 1회 조회
 * - 맵이 켜져 있는 동안 연속 위치 업데이트
 * - 지도 카메라 위치 관리
 */
@HiltViewModel
class LocationMapViewModel(
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationMapUiState())
    val uiState: StateFlow<LocationMapUiState> = _uiState

    private var continuousJob: Job? = null

    /**
     * 위치 권한이 허용된 상태에서 한 번만 현재 위치를 조회한다.
     *
     * 성공 시:
     * - currentLat/currentLng 갱신
     * - cameraLat/cameraLng를 현재 위치로 초기화
     *
     * 실패 시:
     * - locationError 에 에러 메시지 설정
     */
    fun loadCurrentLocationOnce() {
        _uiState.value = _uiState.value.copy(
            isLoadingLocation = true,
            locationError = null
        )
        viewModelScope.launch {
            try {
                val location = locationProvider.getCurrentLocationOnce()
                val lat = location.latitude
                val lng = location.longitude

                _uiState.value = _uiState.value.copy(
                    isLoadingLocation = false,
                    currentLat = lat,
                    currentLng = lng,
                    cameraLat = lat,
                    cameraLng = lng
                )
            } catch (e: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoadingLocation = false,
                    locationError = e.message ?: "위치 조회 실패"
                )
            }
        }
    }

    /**
     * 지도 카메라가 이동했을 때 호출된다.
     *
     * @param lat 카메라 중심 위도
     * @param lng 카메라 중심 경도
     */
    fun onCameraMoved(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(
            cameraLat = lat,
            cameraLng = lng
        )
    }

    /**
     * "내 위치" 버튼 클릭 시, 카메라를 현재 위치로 이동시키기 위해
     * cameraLat/cameraLng를 currentLat/currentLng로 되돌린다.
     */
    fun moveCameraToCurrentLocation() {
        val lat = _uiState.value.currentLat
        val lng = _uiState.value.currentLng
        if (lat != null && lng != null) {
            _uiState.value = _uiState.value.copy(
                cameraLat = lat,
                cameraLng = lng
            )
        }
    }

    /**
     * 맵이 켜져 있는 동안 주기적으로 위치 업데이트를 시작한다.
     *
     * - 기존 연속 위치 Job이 있으면 취소 후 새로 시작
     * - 위치가 들어올 때마다 currentLat/currentLng를 갱신
     * - 필요 시 cameraLat/cameraLng도 함께 갱신하도록 확장 가능
     */
    fun startContinuousLocation() {
        continuousJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isLoadingLocation = true,
            locationError = null
        )

        continuousJob = viewModelScope.launch {
            locationProvider.locationUpdatesFlow()
                .collect { loc ->
                    val lat = loc.latitude
                    val lng = loc.longitude
                    _uiState.value = _uiState.value.copy(
                        isLoadingLocation = false,
                        currentLat = lat,
                        currentLng = lng
                        // 필요하면 cameraLat/cameraLng도 같이 업데이트
                    )
                }
        }
    }

    /**
     * 연속 위치 업데이트를 중단한다.
     *
     * Fragment의 onStop 등에서 호출해 배터리 소모를 줄인다.
     */
    fun stopContinuousLocation() {
        continuousJob?.cancel()
        continuousJob = null
    }

    /**
     * ViewModel이 파괴될 때 연속 위치 Job도 정리한다.
     */
    override fun onCleared() {
        super.onCleared()
        continuousJob?.cancel()
    }
}
