package com.umc.hellodoctor.feature.drug.data.ocr

import com.umc.hellodoctor.feature.drug.domain.model.OcrResult

class PrescriptionTextParser {
    companion object {
        private val MEDICINE_SUFFIX_REGEX =
            Regex(
                """[가-힣a-zA-Z0-9\s]+(?:정|캡슐|시럽|액|연고|크림|주사|패치|산|환|침|좌제|흡입제)(?:\([^)]*\))?""",
            )
        private val FREQUENCY_REGEX = Regex("""1일\s*(\d+)\s*회""")
        private val DURATION_REGEX = Regex("""(\d+)\s*일\s*(?:분|간|치)""")
        private val DOSAGE_REGEX = Regex("""1회\s*(\d+(?:\.\d+)?)\s*(?:정|캡슐|포|ml)""")
        private const val MIN_MEDICINE_NAME_LENGTH = 3
    }

    fun parse(rawText: String): OcrResult {
        val medicines =
            MEDICINE_SUFFIX_REGEX.findAll(rawText)
                .map { it.value.trim() }
                .filter { it.length >= MIN_MEDICINE_NAME_LENGTH }
                .distinct()
                .toList()
        return OcrResult(
            rawText = rawText,
            recognizedMedicineNames = medicines,
            frequencyHint = FREQUENCY_REGEX.find(rawText)?.groupValues?.get(1)?.let { "${it}회/일" },
            dosageHint = DOSAGE_REGEX.find(rawText)?.value,
            durationDays = DURATION_REGEX.find(rawText)?.groupValues?.get(1)?.toIntOrNull(),
        )
    }
}
