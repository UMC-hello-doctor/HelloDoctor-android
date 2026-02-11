package com.umc.hellodoctor.feature.chat.domain.model

import com.umc.hellodoctor.feature.chat.data.ai.SymptomSummaryResponse

/**
 * 채팅 세션 - 질문, 답변, 추천 진료과, 증상 요약 관리
 */
data class ChatSession(
    val id: String = "",
    val questions: MutableList<String> = mutableListOf(),        // 사용자 질문들
    val answers: MutableList<String> = mutableListOf(),          // 봇 답변들
    val recommendedDepartments: MutableList<String> = mutableListOf(), // 추천 진료과
    var symptomSummaryResponse: SymptomSummaryResponse? = null,   // 증상 요약 (JSON 객체)
    val createdAt: Long = System.currentTimeMillis()
) {
    // Q&A 쌍 추가
    fun addQA(question: String, answer: String) {
        questions.add(question)
        answers.add(answer)
    }

    // 추천 진료과 추가
    fun addRecommendedDepartment(department: String) {
        if (!recommendedDepartments.contains(department)) {
            recommendedDepartments.add(department)
        }
    }

    // 증상 요약 업데이트 (JSON 객체)
    fun updateSymptomSummaryResponse(summary: SymptomSummaryResponse) {
        symptomSummaryResponse = summary
    }

    // 전체 Q&A 개수
    fun getQACount() = questions.size

    // 마지막 질문 가져오기
    fun getLastQuestion(): String? = questions.lastOrNull()

    // 마지막 답변 가져오기
    fun getLastAnswer(): String? = answers.lastOrNull()
}
