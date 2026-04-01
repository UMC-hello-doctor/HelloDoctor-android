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
import javax.inject.Inject

@HiltViewModel
class PrescriptionScanViewModel
    @Inject
    constructor(
        private val scanPrescriptionUseCase: ScanPrescriptionUseCase,
    ) : ViewModel() {
        private val _uiState = MutableLiveData<PrescriptionScanUiState>(PrescriptionScanUiState.Idle)
        val uiState: LiveData<PrescriptionScanUiState> get() = _uiState

        fun resetToIdle() {
            _uiState.value = PrescriptionScanUiState.Idle
        }

        fun onCaptureFailed(message: String) {
            _uiState.value = PrescriptionScanUiState.Error(message)
        }

        fun onImageCaptured(uri: Uri) {
            _uiState.value = PrescriptionScanUiState.Loading
            viewModelScope.launch {
                scanPrescriptionUseCase(uri)
                    .onSuccess { _uiState.value = PrescriptionScanUiState.Success(it) }
                    .onFailure { _uiState.value = PrescriptionScanUiState.Error(it.message ?: "인식 실패") }
            }
        }
    }

sealed class PrescriptionScanUiState {
    object Idle : PrescriptionScanUiState()

    object Loading : PrescriptionScanUiState()

    data class Success(val result: OcrResult) : PrescriptionScanUiState()

    data class Error(val message: String) : PrescriptionScanUiState()
}
