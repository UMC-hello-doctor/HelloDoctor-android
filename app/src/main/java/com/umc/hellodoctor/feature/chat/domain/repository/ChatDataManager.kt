package com.umc.hellodoctor.feature.chat.domain.repository

import android.util.Log
import com.umc.hellodoctor.feature.chat.data.ai.AiRepository
import com.umc.hellodoctor.feature.chat.data.ai.DepartmentRecommendation
import com.umc.hellodoctor.feature.chat.domain.model.ChatSession
import com.umc.hellodoctor.feature.chat.domain.model.Message
import com.umc.hellodoctor.feature.chat.domain.model.MessageType
import javax.inject.Inject

/**
 * ChatRepository - 채팅 세션 저장/조회
 */
interface ChatRepository {
    suspend fun saveChatSession(session: ChatSession)

    suspend fun getChatSession(sessionId: String): ChatSession?

    suspend fun getAllSessions(): List<ChatSession>

    suspend fun deleteChatSession(sessionId: String)
}

/**
 * ChatDataManager - 채팅 도메인 로직
 */
class ChatDataManager
    @Inject
    constructor(
        private val aiRepository: AiRepository?,
    ) {
        /**
         * Gemini를 통한 증상 요약 생성
         * @param questions 질문 리스트
         * @param answers 답변 리스트
         * @return 증상 요약 JSON 객체
         */
        suspend fun generateSymptomSummaryWithAI(
            questions: List<String>,
            answers: List<String>,
        ): com.umc.hellodoctor.feature.chat.data.ai.SymptomSummaryResponse {
            Log.d(TAG, "--- ChatDataManager.generateSymptomSummaryWithAI 시작 ---")
            Log.d(TAG, "입력 질문 개수: ${questions.size}")
            Log.d(TAG, "입력 답변 개수: ${answers.size}")

            return try {
                if (aiRepository == null) {
                    Log.w(TAG, "AiRepository가 null입니다. 빈 객체 반환")
                    return com.umc.hellodoctor.feature.chat.data.ai.SymptomSummaryResponse()
                }

                Log.d(TAG, "AiRepository.generateSymptomSummaryWithAI 호출")
                val result = aiRepository.generateSymptomSummaryWithAI(questions, answers)

                val summary = result.getOrNull()
                if (summary != null) {
                    Log.d(TAG, "AI 증상 요약 생성 성공")
                    Log.d(TAG, "요약 길이: ${summary.original?.data?.size ?: 0}개 항목")
                    summary
                } else {
                    Log.w(TAG, "AI 결과가 null입니다. 빈 객체 반환")
                    Log.w(TAG, "Result exceptionOrNull: ${result.exceptionOrNull()?.message}")
                    com.umc.hellodoctor.feature.chat.data.ai.SymptomSummaryResponse()
                }
            } catch (e: Exception) {
                Log.e(TAG, "AI 증상 요약 생성 실패: ${e.message}", e)
                Log.e(TAG, "예외 타입: ${e.javaClass.simpleName}")
                com.umc.hellodoctor.feature.chat.data.ai.SymptomSummaryResponse()
            } finally {
                Log.d(TAG, "--- ChatDataManager.generateSymptomSummaryWithAI 종료 ---")
            }
        }

        /**
         * 질문 목록으로부터 증상 요약 생성 (폴백 - 기존 방식)
         */
        private fun generateSymptomSummaryFallback(
            questions: List<String>,
            answers: List<String>,
        ): String {
            return buildString {
                appendLine("=== 증상 요약 ===")
                appendLine()

                // Q&A 목록 출력
                if (questions.isNotEmpty()) {
                    appendLine("📋 진료 기록:")
                    questions.forEachIndexed { index, question ->
                        appendLine("Q${index + 1}: $question")
                        if (index < answers.size) {
                            appendLine("A${index + 1}: ${answers[index]}")
                        }
                        appendLine()
                    }
                }
            }
        }

        /**
         * ChatSession을 사용한 증상 요약 생성 (하위 호환성 유지)
         */
        fun generateSymptomSummary(session: ChatSession): String {
            return buildString {
                appendLine("=== 증상 요약 ===")
                appendLine()

                // null-safe 처리
                val questions = session.questions.toList()
                val answers = session.answers.toList()

                // Q&A 목록 출력
                if (questions.isNotEmpty()) {
                    appendLine("📋 진료 기록:")
                    questions.forEachIndexed { index, question ->
                        appendLine("Q${index + 1}: $question")
                        if (index < answers.size) {
                            appendLine("A${index + 1}: ${answers[index]}")
                        }
                        appendLine()
                    }
                }

                // 추천 진료과 출력 (null-safe)
                val departments = session.recommendedDepartments.filterNotNull()
                if (departments.isNotEmpty()) {
                    appendLine("🏥 추천 진료과:")
                    departments.forEach { dept ->
                        appendLine("• $dept")
                    }
                }
            }
        }

        /**
         * Gemini 상세 추천 결과 반환
         */
        suspend fun recommendDepartmentDetailed(symptoms: List<String>): DepartmentRecommendation? {
            val combined = symptoms.joinToString(". ") { it }
            return aiRepository?.recommendDepartmentDetailed(combined)?.getOrNull()
        }

        /**
         * 사용자 입력에 따른 진료과 추천
         * Gemini 호출을 시도하고 실패 시 로컬 폴백(정규식)을 사용합니다.
         */
        suspend fun recommendDepartments(symptoms: List<String>): List<String> {
            val combined = symptoms.joinToString(". ") { it }

            // AI 호출 시도
            aiRepository?.let { repo ->
                try {
                    val detailed = repo.recommendDepartmentDetailed(combined).getOrNull()
                    if (detailed != null && detailed.departmentKo.isNotBlank()) {
                        return listOf(detailed.department)
                    }
                } catch (e: Exception) {
                    // AI 호출 실패 시 로컬 폴백으로 진행
                }
            }

            // 로컬 폴백
            return fallbackRecommendDepartments(symptoms)
        }

        private fun fallbackRecommendDepartments(symptoms: List<String>): List<String> {
            val departments = mutableSetOf<String>()

            symptoms.forEach { symptom ->
                when {
                    // 두통, 어지러움, 현기증
                    Regex("두통|어지러움|현기증", RegexOption.IGNORE_CASE).containsMatchIn(symptom) -> {
                        departments.add("신경과")
                        departments.add("뇌신경외과")
                    }
                    // 기침/호흡
                    Regex("기침|가래|호흡곤란|천식", RegexOption.IGNORE_CASE).containsMatchIn(symptom) -> {
                        departments.add("호흡기내과")
                        departments.add("폐질환클리닉")
                    }
                    // 소화
                    Regex("소화|속쓰림|복통|설사|변비", RegexOption.IGNORE_CASE).containsMatchIn(symptom) -> {
                        departments.add("소화기내과")
                        departments.add("위장관클리닉")
                    }
                    // 피부
                    Regex("피부|여드름|습진|가려움", RegexOption.IGNORE_CASE).containsMatchIn(symptom) -> {
                        departments.add("피부과")
                    }
                    // 관절/통증
                    Regex("관절|뼈|척추|통증", RegexOption.IGNORE_CASE).containsMatchIn(symptom) -> {
                        departments.add("정형외과")
                        departments.add("척추전문클리닉")
                    }
                    // 심장
                    Regex("심장|심계항진|흉통", RegexOption.IGNORE_CASE).containsMatchIn(symptom) -> {
                        departments.add("순환기내과")
                        departments.add("심장내과")
                    }
                    // 피로
                    Regex("피로|무기력|힘없음", RegexOption.IGNORE_CASE).containsMatchIn(symptom) -> {
                        departments.add("내과")
                        departments.add("종합검진")
                    }
                }
            }

            return departments.toList()
        }

        /**
         * 채팅 세션의 통계 정보
         */
        fun getSessionStatistics(session: ChatSession): Map<String, Any> {
            return mapOf(
                "totalQA" to session.getQACount(),
                "questionCount" to session.questions.size,
                "answerCount" to session.answers.size,
                "recommendedDepartmentCount" to session.recommendedDepartments.size,
                "hasSymptomSummary" to (session.symptomSummaryResponse?.original?.data?.isNotEmpty() == true),
                "createdAt" to session.createdAt,
                "duration" to (System.currentTimeMillis() - session.createdAt),
            )
        }

        /**
         * 메시지 타입별 카운트
         */
        fun countMessagesByType(messages: List<Message>): Map<MessageType, Int> {
            return messages.groupingBy { it.type }
                .eachCount()
        }

        companion object {
            private const val TAG = "ChatDataManager"
        }
    }
