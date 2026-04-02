package com.umc.hellodoctor.feature.drug.presentation

import android.net.Uri
import android.util.Log
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
        companion object {
            private const val TAG = "PrescriptionScanVM"
        }

        private val _uiState = MutableLiveData<PrescriptionScanUiState>(PrescriptionScanUiState.Idle)
        val uiState: LiveData<PrescriptionScanUiState> get() = _uiState

        fun onImageCaptured(uri: Uri) {
            _uiState.value = PrescriptionScanUiState.Loading
            viewModelScope.launch {
                scanPrescriptionUseCase(uri)
                    .onSuccess { result ->
                        Log.d(
                            TAG,
                            "사용자에게 결과 표시: " +
                                "${result.recognizedMedicineNames.size}개 약명",
                        )
                        _uiState.value = PrescriptionScanUiState.Success(result)
                    }
                    .onFailure { exception ->
                        val errorMessage = exception.message ?: "약명 인식 실패"
                        Log.d(TAG, "사용자에게 에러 표시: $errorMessage")
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
