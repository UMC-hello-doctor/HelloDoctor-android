package com.umc.hellodoctor.feature.drug.domain.model

data class OcrResult(
    val rawText: String,
    val recognizedMedicineNames: List<String>,
    val dosageHint: String? = null,
    val frequencyHint: String? = null,
    val durationDays: Int? = null,
)
