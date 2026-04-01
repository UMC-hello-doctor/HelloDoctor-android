package com.umc.hellodoctor.feature.chat.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.umc.hellodoctor.feature.chat.data.ai.SymptomSummaryResponse
import com.umc.hellodoctor.feature.chat.domain.model.ChatSession

/**
 * Room Entity - ChatSession 저장용
 */
@Entity(tableName = "chat_sessions")
@TypeConverters(Converters::class)
data class ChatSessionEntity(
    @PrimaryKey
    val id: String,
    val questions: List<String>,
    val answers: List<String>,
    val recommendedDepartments: List<String>,
    val symptomSummaryResponse: SymptomSummaryResponse?,
    val createdAt: Long = System.currentTimeMillis(),
) {
    /**
     * Entity -> Domain Model 변환
     */
    fun toDomain(): ChatSession {
        return ChatSession(
            id = id,
            questions = questions.toMutableList(),
            answers = answers.toMutableList(),
            recommendedDepartments = recommendedDepartments.toMutableList(),
            symptomSummaryResponse = symptomSummaryResponse,
            createdAt = createdAt,
        )
    }

    companion object {
        /**
         * Domain Model -> Entity 변환
         */
        fun fromDomain(session: ChatSession): ChatSessionEntity {
            return ChatSessionEntity(
                id = session.id,
                questions = session.questions.toList(),
                answers = session.answers.toList(),
                recommendedDepartments = session.recommendedDepartments.toList(),
                symptomSummaryResponse = session.symptomSummaryResponse,
                createdAt = session.createdAt,
            )
        }
    }
}
