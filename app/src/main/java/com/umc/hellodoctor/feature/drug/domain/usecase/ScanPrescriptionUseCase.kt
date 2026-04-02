package com.umc.hellodoctor.feature.drug.domain.usecase

import android.net.Uri
import com.umc.hellodoctor.feature.drug.domain.model.OcrResult
import com.umc.hellodoctor.feature.drug.domain.repository.OcrRepository
import javax.inject.Inject

class ScanPrescriptionUseCase
    @Inject
    constructor(
        private val ocrRepository: OcrRepository,
    ) {
        suspend operator fun invoke(imageUri: Uri): Result<OcrResult> = ocrRepository.recognizeText(imageUri)
    }
