package com.umc.hellodoctor.feature.chat.domain.model

// 메시지 타입
enum class MessageType {
    QUESTION, // 질문
    ANSWER, // 답변
    RECOMMENDATION, // 추천 진료과
    SYMPTOM_SUMMARY, // 증상 요약
    EMERGENCY, // 응급 경고
}

data class Message(
    val text: String,
    val isBot: Boolean,
    val type: MessageType = if (isBot) MessageType.ANSWER else MessageType.QUESTION,
    // 응급 메시지 여부
    val isEmergency: Boolean = false,
)
