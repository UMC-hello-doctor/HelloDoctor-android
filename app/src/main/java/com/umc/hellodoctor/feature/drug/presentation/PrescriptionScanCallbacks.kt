package com.umc.hellodoctor.feature.drug.presentation

import android.net.Uri
import com.umc.hellodoctor.feature.drug.domain.model.OcrResult

data class PrescriptionScanCallbacks(
    val onImageCaptured: (Uri) -> Unit,
    val onCaptureFailed: (String) -> Unit,
    val onViewResult: (OcrResult) -> Unit,
    val onRetake: () -> Unit,
    val onBack: () -> Unit,
)
