package com.umc.hellodoctor.feature.chat.data.ai

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import javax.inject.Inject

/**
 * AiRepository - Gemini API를 호출하여 진료과 추천 및 상세 분석
 */
class AiRepository
    @Inject
    constructor(
        private val service: AiService,
        private val gson: Gson,
        private val apiKey: String? = null,
    ) {
        private val tag = "AiRepository"

        /**
         * 상세 진료과 추천 (JSON 구조화 응답)
         */
        @Suppress("ReturnCount", "TooGenericExceptionCaught")
        suspend fun recommendDepartmentDetailed(symptom: String): Result<DepartmentRecommendation> {
            if (apiKey.isNullOrBlank()) {
                return Result.failure(Exception("API 키가 설정되지 않았습니다"))
            }

            return try {
                val systemPrompt = buildDetailedSystemPrompt()
                val request =
                    GeminiGenerateRequest(
                        contents =
                            listOf(
                                Content(
                                    role = "user",
                                    parts = listOf(Part(text = symptom)),
                                ),
                            ),
                        systemInstruction =
                            SystemInstruction(
                                parts = listOf(Part(text = systemPrompt)),
                            ),
                        generationConfig =
                            GenerationConfig(
                                temperature = 0.3,
                                topP = 0.95,
                                topK = 64,
                                maxOutputTokens = 1024,
                            ),
                        safetySettings =
                            listOf(
                                SafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_ONLY_HIGH"),
                                SafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_ONLY_HIGH"),
                                SafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_MEDIUM_AND_ABOVE"),
                                SafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_ONLY_HIGH"),
                            ),
                    )

                Log.d(tag, "상세 추천 API 호출 시작")
                Log.d(tag, "recommendDepartmentDetailed: $request")
                val response = service.generateContent(apiKey, request)

                val jsonText =
                    response.candidates?.firstOrNull()
                        ?.content?.parts?.firstOrNull()
                        ?.text ?: return Result.failure(Exception("응답 없음"))

                Log.d(tag, "상세 응답: $jsonText")

                val recommendation = parseRecommendation(jsonText)
                Log.d(tag, "파싱된 추천: $recommendation")

                Result.success(recommendation)
            } catch (e: Exception) {
                Log.e(tag, "상세 추천 오류: ${e.message}", e)
                Result.failure(e)
            }
        }

        /**
         * 대화 이력 포함 메시지 전송
         * chatHistory: list of (role, text) pairs
         */
        @Suppress("ReturnCount", "TooGenericExceptionCaught")
        suspend fun sendMessageWithHistory(
            chatHistory: List<Pair<String, String>>,
            newMessage: String,
        ): Result<String> {
            Log.d(tag, "--- sendMessageWithHistory 시작 ---")
            Log.d(tag, "대화 이력 개수: ${chatHistory.size}")
            Log.d(tag, "새 메시지: ${newMessage.take(LOG_PREVIEW_LENGTH)}...")

            if (apiKey.isNullOrBlank()) {
                Log.e(tag, "API 키가 설정되지 않았습니다")
                return Result.failure(Exception("API 키가 설정되지 않았습니다"))
            }

            return try {
                val contents =
                    chatHistory.map { (role, text) ->
                        Content(role = role, parts = listOf(Part(text = text)))
                    } + Content(role = "user", parts = listOf(Part(text = newMessage)))

                Log.d(tag, "총 Content 개수: ${contents.size}")

                val request =
                    GeminiGenerateRequest(
                        contents = contents,
                        generationConfig =
                            GenerationConfig(
                                temperature = MESSAGE_TEMPERATURE,
                                maxOutputTokens = MESSAGE_MAX_OUTPUT_TOKENS,
                            ),
                    )

                Log.d(tag, "Gemini API 호출 중...")
                val response = service.generateContent(apiKey, request)

                Log.d(tag, "API 응답 수신")
                Log.d(tag, "Candidates 개수: ${response.candidates?.size ?: 0}")

                val text =
                    response.candidates?.firstOrNull()
                        ?.content?.parts?.firstOrNull()
                        ?.text

                if (text == null) {
                    Log.e(tag, "응답 텍스트가 null입니다")
                    return Result.failure(Exception("응답 없음"))
                }

                Log.d(tag, "응답 텍스트 길이: ${text.length}자")
                Log.d(tag, "응답 텍스트: ${text.take(LOG_RESPONSE_PREVIEW_LENGTH)}...")
                Log.d(tag, "--- sendMessageWithHistory 성공 ---")

                Result.success(text)
            } catch (e: Exception) {
                Log.e(tag, "대화 전송 오류: ${e.message}", e)
                Log.e(tag, "예외 타입: ${e.javaClass.simpleName}")
                Log.e(tag, "StackTrace: ${e.stackTraceToString().take(LOG_STACKTRACE_PREVIEW_LENGTH)}")
                Result.failure(e)
            }
        }

        /**
         * Gemini 증상 요약 생성 (질문/답변 기반)
         */
        suspend fun generateSymptomSummaryWithAI(
            questions: List<String>,
            answers: List<String>,
        ): Result<SymptomSummaryResponse> {
            Log.d(tag, "=== AiRepository.generateSymptomSummaryWithAI 시작 ===")
            Log.d(tag, "질문 개수: ${questions.size}, 답변 개수: ${answers.size}")

            // 질문과 답변을 대화 이력으로 변환
            val chatHistory = mutableListOf<Pair<String, String>>()
            questions.forEachIndexed { index, question ->
                Log.d(tag, "History[$index] - Q: $question")
                chatHistory.add("model" to question)
                if (index < answers.size) {
                    Log.d(tag, "History[$index] - A: ${answers[index]}")
                    chatHistory.add("user" to answers[index])
                }
            }

            Log.d(tag, "대화 이력 총 ${chatHistory.size}개 항목 생성")

            val summaryPrompt = buildSymptomSummaryPrompt()
            Log.d(tag, "증상 요약 프롬프트 생성 완료")
            Log.d(tag, "프롬프트 길이: ${summaryPrompt.length}자")

            Log.d(tag, "sendMessageWithHistory 호출 중...")
            val result =
                sendMessageWithHistory(
                    chatHistory = chatHistory,
                    newMessage = summaryPrompt,
                )

            if (result.isSuccess) {
                Log.d(tag, "증상 요약 생성 성공")
                Log.d(tag, "생성된 응답 길이: ${result.getOrNull()?.length}자")
                result.getOrNull()?.let { response ->
                    Log.d(tag, "생성된 응답: ${response.take(LOG_RESPONSE_PREVIEW_LENGTH)}...")
                }
            } else {
                Log.e(tag, "증상 요약 생성 실패: ${result.exceptionOrNull()?.message}")
            }

            Log.d(tag, "=== AiRepository.generateSymptomSummaryWithAI 종료 ===")

            return result.mapCatching { jsonResponse ->
                parseSymptomSummary(jsonResponse)
            }
        }

        // ========== Private Helper Methods =========

        private fun buildDetailedSystemPrompt(): String {
            return """
                You are medical department recommendation AI. Analyze symptoms and respond ONLY in JSON format.
                Always respond in the SAME language as user input (Korean, English, Japanese, Chinese, etc.).

                **Departments(25):** Internal Medicine, Pediatrics, Neurology, Psychiatry, Dermatology, Surgery, Thoracic Surgery, Orthopedics, Nuclear Medicine, Neurosurgery, Plastic Surgery, Obstetrics & Gynecology, Anesthesiology, ENT, Urology, Rehabilitation, Pathology, Radiology, Radiation Oncology, Clinical Pathology, Dentistry, Family Medicine, Emergency Medicine, Oral & Maxillofacial Surgery, Ophthalmology

                **Red Flags (Emergency):**
                - Internal Medicine: chest pain radiating to arm, cyanosis, severe dyspnea, black stool/vomiting blood, rigid abdomen, altered consciousness, fever>39°C
                - Neurology/Neurosurgery: unilateral paralysis, facial droop, slurred speech, sudden severe headache, seizure, stiff neck+fever
                - Ophthalmology: sudden vision loss, partial visual field loss, severe eye pain+vomiting
                - ENT: difficulty swallowing, neck swelling+can't open mouth, stridor
                - Dermatology: urticaria+dyspnea, lip/eye swelling+syncope
                - Urology: flank pain+fever>38°C, urinary retention, gross hematuria
                - Ob/Gyn: heavy bleeding(pad soaked in 15min), pregnancy+sharp abdominal pain+cold sweat, pregnancy bleeding
                - Orthopedics: bowel/bladder incontinence+leg numbness, fracture+pale fingers/toes, open fracture
                - Surgery/Thoracic: abdominal trauma+severe pain, chest trauma+dyspnea
                - Dentistry/OMFS: tooth pain+facial/neck swelling+dyspnea, jaw fracture
                - Psychiatry: suicide attempt, self-harm
                - Pediatrics: infant<3mo fever>38°C, pediatric seizure, dyspnea+cyanosis

                **JSON Format:**
                {
                  "department": "Internal Medicine",
                  "departmentKo": "내과",
                  "departmentEn": "Internal Medicine",
                  "confidence": 0.85,
                  "reason": "Analysis in user's input language",
                  "nextQuestions": ["Q1 in user's language", "Q2", "Q3"],
                  "isEmergency": false,
                  "emergencyReason": null
                }

                **Rules:**
                1. Output JSON ONLY
                2. If Red Flag detected: isEmergency=true
                3. confidence: 0.0~1.0 (lower when vague)
                4. reason/nextQuestions in user's input language

                **Question Generation (PQRST):**
                Generate 3-5 questions, each from DIFFERENT category:

                T(Timing-MANDATORY): When started/How long/Sudden or gradual/Constant or intermittent
                R(Region): Where exactly/Does it spread
                Q(Quality): How does it feel (sharp/dull/burning)
                S(Severity): Scale 1-10/Interferes with daily life
                P(Provocation): What makes better/worse/Triggering activities
                A(Associated): Other symptoms/Fever, nausea, etc.

                **Never ask 2 questions from same category. T is mandatory.**
                """.trimIndent()
        }

        private fun buildSymptomSummaryPrompt(): String {
            return """
                Convert user's Q&A responses into dual-language JSON format:
                1. Original language (with language code)
                2. Korean translation

                Question-Answer pairs:
                1. Q: [question1]
                   A: [answer1]
                2. Q: [question2]
                   A: [answer2]
                3. Q: [question3]
                   A: [answer3]

                Output format:
                {
                  "original": {
                    "language": "en",
                    "languageName": "English",
                    "data": [
                      {"category": "short phrase", "description": "answer1 original text"},
                      {"category": "short phrase", "description": "answer2 original text"}
                    ]
                  },
                  "korean": [
                    {"category": "짧은구", "description": "답변1 (Korean)"},
                    {"category": "짧은구", "description": "답변2 (Korean)"}
                  ]
                }

                Requirements:
                - original.language: user input language code (en, ja, zh, ko, etc.)
                - original.languageName: language name (English, Japanese, Chinese, Korean, etc.)
                - original.data: keep user's input language
                - korean: translate to Korean
                - category: 1-3 word keyword/phrase representing question core (no full sentences)
                - description: full answer text (sentences allowed)
                - exclude unanswered questions
                - output JSON object only
                - return JSON only, no additional text

                """.trimIndent()
        }

        private fun parseRecommendation(jsonText: String): DepartmentRecommendation {
            // JSON 코드 블록 제거 (```json ... ``` 형식)
            val cleanJson =
                jsonText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

            return try {
                gson.fromJson(cleanJson, DepartmentRecommendation::class.java)
            } catch (e: JsonSyntaxException) {
                Log.e(tag, "JSON 파싱 실패, 기본값 반환: ${e.message}")
                // 폴백: 기본 응답 반환
                DepartmentRecommendation(
                    department = "Unknown",
                    departmentKo = "알 수 없음",
                    departmentEn = "Unknown",
                    confidence = 0.0,
                    reason = "응답을 파싱할 수 없습니다.",
                    nextQuestions = emptyList(),
                    isEmergency = false,
                    emergencyReason = null,
                )
            }
        }

        /**
         * 증상 요약 JSON 파싱
         */
        private fun parseSymptomSummary(jsonText: String): SymptomSummaryResponse {
            val cleanJson =
                jsonText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

            return try {
                gson.fromJson(cleanJson, SymptomSummaryResponse::class.java)
            } catch (e: JsonSyntaxException) {
                Log.e(tag, "증상 요약 JSON 파싱 실패: ${e.message}")
                // 폴백: 빈 응답 반환
                SymptomSummaryResponse()
            }
        }

        companion object {
            private const val LOG_PREVIEW_LENGTH = 100
            private const val LOG_RESPONSE_PREVIEW_LENGTH = 200
            private const val LOG_STACKTRACE_PREVIEW_LENGTH = 500
            private const val MESSAGE_TEMPERATURE = 0.7
            private const val MESSAGE_MAX_OUTPUT_TOKENS = 2048
        }
    }
