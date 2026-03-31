package com.umc.hellodoctor.core.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocationMapUiState(
    val isLoadingLocation: Boolean = false,
    val currentLat: Double? = null,
    val currentLng: Double? = null,
    val currentBearing: Float? = null,
    val cameraLat: Double? = null,
    val cameraLng: Double? = null,
    val locationError: String? = null,
)

/**
 * 지도 화면에서 위치 상태를 관리하는 ViewModel.
 *
 * - 현재 위치 1회 조회
 * - 맵이 켜져 있는 동안 연속 위치 업데이트
 * - 지도 카메라 위치 관리
 * - 마지막 위치는 내부 변수로만 보관 (Flow/X)
 */
@HiltViewModel
class LocationMapViewModel
    @Inject
    constructor(
        private val locationProvider: LocationProvider,
        private val bearingProvider: BearingProvider,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LocationMapUiState())
        val uiState: StateFlow<LocationMapUiState> = _uiState

        private var continuousJob: Job? = null

        /** 내부에서만 쓰는 마지막 위치 (UI에는 직접 노출하지 않음) */
        private var lastLat: Double? = null
        private var lastLng: Double? = null

        /**
         * 위치 권한이 허용된 상태에서 한 번만 현재 위치를 조회한다.
         *
         * 성공 시:
         * - currentLat/currentLng 갱신
         * - cameraLat/cameraLng를 현재 위치로 초기화
         * - lastLat/lastLng에 현재 위치 저장 (Flow로 공개하지 않음)
         */
        fun loadCurrentLocationOnce() {
            _uiState.value =
                _uiState.value.copy(
                    isLoadingLocation = true,
                    locationError = null,
                )
            viewModelScope.launch {
                try {
                    val location = locationProvider.getCurrentLocationOnce()
                    val lat = location.latitude
                    val lng = location.longitude
                    val bearing = location.bearing

                    // 내부 last 위치 기록
                    lastLat = lat
                    lastLng = lng

                    _uiState.value =
                        _uiState.value.copy(
                            isLoadingLocation = false,
                            currentLat = lat,
                            currentLng = lng,
                            currentBearing = bearing,
                            cameraLat = lat,
                            cameraLng = lng,
                        )
                } catch (e: Throwable) {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoadingLocation = false,
                            locationError = e.message ?: "위치 조회 실패",
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
        fun onCameraMoved(
            lat: Double,
            lng: Double,
        ) {
            _uiState.value =
                _uiState.value.copy(
                    cameraLat = lat,
                    cameraLng = lng,
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
                _uiState.value =
                    _uiState.value.copy(
                        cameraLat = lat,
                        cameraLng = lng,
                    )
            }
        }

        /**
         * 맵이 켜져 있는 동안 주기적으로 위치 업데이트를 시작한다.
         *
         * - 기존 연속 위치 Job이 있으면 취소 후 새로 시작
         * - 위치가 들어올 때마다 currentLat/currentLng를 갱신
         * - 마지막 위치(lastLat/lastLng)는 내부 변수로만 갱신
         */
        fun startContinuousLocation() {
            continuousJob?.cancel()
            _uiState.value =
                _uiState.value.copy(
                    isLoadingLocation = true,
                    locationError = null,
                )

            continuousJob =
                viewModelScope.launch {
                    combine(
                        // Flow<Location>
                        locationProvider.locationUpdatesFlow(),
                        // Flow<Float>
                        bearingProvider.bearingFlow,
                    ) { loc, bearing ->
                        Pair(loc, bearing)
                    }.collect { (loc, bearing) ->
                        val lat = loc.latitude
                        val lng = loc.longitude

                        // 내부 last 위치 업데이트 (UI에는 반영 안 해도 됨)
                        lastLat = lat
                        lastLng = lng

                        _uiState.value =
                            _uiState.value.copy(
                                isLoadingLocation = false,
                                currentLat = lat,
                                currentLng = lng,
                                currentBearing = bearing,
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

        /**
         * 내부에 저장된 마지막 위치를 반환.
         * - 예: 앱 재시작 시 초기 카메라 위치로 사용하고 싶을 때 호출.
         */
        fun getLastLocation(): Pair<Double, Double>? {
            val lat = lastLat
            val lng = lastLng
            return if (lat != null && lng != null) {
                lat to lng
            } else {
                null
            }
        }
    }
