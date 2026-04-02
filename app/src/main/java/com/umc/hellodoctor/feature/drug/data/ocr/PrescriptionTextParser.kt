package com.umc.hellodoctor.feature.drug.data.ocr

import com.umc.hellodoctor.feature.drug.domain.model.OcrResult

class PrescriptionTextParser {
    companion object {
        // Existing pattern: extracts medicine names with suffix
        private val MEDICINE_SUFFIX_REGEX =
            Regex(
                """[가-힣a-zA-Z0-9\s]+(?:정|캡슐|시럽|액|연고|크림|주사|패치|산|환|침|좌제|흡입제)(?:\([^)]*\))?""",
            )

        // New pattern: extracts numbered medicine list format (e.g., "1. Aspirin Tab")
        private val NUMBERED_MEDICINE_REGEX =
            Regex(
                """^\d+[..)]\s*([가-힣a-zA-Z0-9\s]+(?:정|캡슐|시럽|액|연고|크림|주사|패치|산|환|좌제|흡입제))""",
                RegexOption.MULTILINE,
            )

        private val FREQUENCY_REGEX = Regex("""1일\s*(\d+)\s*회""")
        private val DURATION_REGEX = Regex("""(\d+)\s*일\s*(?:분|간|치)""")
        private val DOSAGE_REGEX = Regex("""1회\s*(\d+(?:\.\d+)?)\s*(?:정|캡슐|포|ml)""")
        private const val MIN_MEDICINE_NAME_LENGTH = 3
    }

    fun parse(rawText: String): OcrResult {
        // Normalize raw text: remove extra spaces and spaces between numbers and Korean characters
        val normalized =
            rawText
                .replace(Regex("""[^\S\n]+"""), " ") // Multiple spaces -> single space (keep newlines)
                .replace(Regex("""(\d)\s+([가-힣])"""), "$1$2") // Remove space between number and Korean

        // Extract medicines using suffix pattern
        val medicinesSuffix =
            MEDICINE_SUFFIX_REGEX.findAll(normalized)
                .map { it.value.trim() }
                .filter { it.length >= MIN_MEDICINE_NAME_LENGTH }
                .toSet()

        // Extract medicines using numbered list pattern
        val medicinesNumbered =
            NUMBERED_MEDICINE_REGEX.findAll(normalized)
                .mapNotNull { it.groupValues.getOrNull(1)?.trim() }
                .filter { it.length >= MIN_MEDICINE_NAME_LENGTH }
                .toSet()

        // Combine both patterns and remove duplicates
        val medicines =
            (medicinesSuffix + medicinesNumbered)
                .distinct()
                .toList()

        return OcrResult(
            rawText = normalized,
            recognizedMedicineNames = medicines,
            frequencyHint =
                FREQUENCY_REGEX.find(normalized)?.groupValues?.get(1)
                    ?.let { "${it}회/일" },
            dosageHint = DOSAGE_REGEX.find(normalized)?.value,
            durationDays =
                DURATION_REGEX.find(normalized)?.groupValues?.get(1)
                    ?.toIntOrNull(),
        )
    }
}
