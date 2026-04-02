package com.umc.hellodoctor.feature.drug.presentation

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.hellodoctor.feature.drug.domain.model.OcrResult
import com.umc.hellodoctor.feature.drug.domain.usecase.ScanPrescriptionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PrescriptionScanViewModel
    @Inject
    constructor(
        private val scanPrescriptionUseCase: ScanPrescriptionUseCase,
    ) : ViewModel() {
        private val _uiState = MutableLiveData<PrescriptionScanUiState>(PrescriptionScanUiState.Idle)
        val uiState: LiveData<PrescriptionScanUiState> get() = _uiState

        fun onImageCaptured(uri: Uri) {
            _uiState.value = PrescriptionScanUiState.Loading
            viewModelScope.launch {
                scanPrescriptionUseCase(uri)
                    .onSuccess { _uiState.value = PrescriptionScanUiState.Success(it) }
                    .onFailure { exception ->
                        val errorMessage =
                            when {
                                exception.message?.contains("시간 초과") == true ->
                                    "분석 시간 초과\n다시 촬영해주세요"
                                exception is IOException ->
                                    "이미지를 읽을 수 없습니다"
                                exception.message?.contains("Invalid URI") == true ->
                                    "유효하지 않은 이미지입니다"
                                else ->
                                    exception.message ?: "약명 인식 실패"
                            }
                        _uiState.value = PrescriptionScanUiState.Error(errorMessage)
                    }
            }
        }

        fun resetToIdle() {
            _uiState.value = PrescriptionScanUiState.Idle
        }
    }

sealed class PrescriptionScanUiState {
    object Idle : PrescriptionScanUiState()

    object Loading : PrescriptionScanUiState()

    data class Success(val result: OcrResult) : PrescriptionScanUiState()

    data class Error(val message: String) : PrescriptionScanUiState()
}
