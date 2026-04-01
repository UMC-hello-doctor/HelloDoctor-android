package com.umc.hellodoctor.feature.drug.domain.repository

import android.net.Uri
import com.umc.hellodoctor.feature.drug.domain.model.OcrResult

interface OcrRepository {
    suspend fun recognizeText(imageUri: Uri): Result<OcrResult>
}
