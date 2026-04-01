package com.umc.hellodoctor.feature.chat.data.repository

import android.util.Log
import com.umc.hellodoctor.feature.chat.data.local.ChatSessionDao
import com.umc.hellodoctor.feature.chat.data.local.ChatSessionEntity
import com.umc.hellodoctor.feature.chat.domain.model.ChatSession
import com.umc.hellodoctor.feature.chat.domain.repository.ChatRepository
import javax.inject.Inject

/**
 * ChatRepository 구현체 - Room DB 사용
 */
class ChatRepositoryImpl
    @Inject
    constructor(
        private val chatSessionDao: ChatSessionDao,
    ) : ChatRepository {
        /**
         * 채팅 세션 저장
         */
        @Suppress("TooGenericExceptionCaught")
        override suspend fun saveChatSession(session: ChatSession) {
            try {
                Log.d(TAG, "세션 저장 시작: ${session.id}")
                Log.d(TAG, "생성 시간: ${session.createdAt}")
                Log.d(TAG, "질문 개수: ${session.questions.size}")
                Log.d(TAG, "답변 개수: ${session.answers.size}")

                val entity = ChatSessionEntity.fromDomain(session)
                chatSessionDao.insertSession(entity)

                Log.d(TAG, "세션 저장 완료: ${session.id}")
            } catch (e: Exception) {
                Log.e(TAG, "세션 저장 실패: ${e.message}", e)
                throw e
            }
        }

        /**
         * 세션 ID로 조회
         */
        @Suppress("TooGenericExceptionCaught")
        override suspend fun getChatSession(sessionId: String): ChatSession? {
            return try {
                Log.d(TAG, "세션 조회: $sessionId")
                val entity = chatSessionDao.getSessionById(sessionId)
                entity?.toDomain()?.also {
                    Log.d(TAG, "세션 조회 성공: $sessionId")
                } ?: run {
                    Log.d(TAG, "세션을 찾을 수 없음: $sessionId")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "세션 조회 실패: ${e.message}", e)
                null
            }
        }

        /**
         * 모든 세션 조회 (최신순)
         */
        @Suppress("TooGenericExceptionCaught")
        override suspend fun getAllSessions(): List<ChatSession> {
            return try {
                Log.d(TAG, "모든 세션 조회")
                val entities = chatSessionDao.getAllSessions()
                val sessions = entities.map { it.toDomain() }
                Log.d(TAG, "총 ${sessions.size}개 세션 조회됨")
                sessions
            } catch (e: Exception) {
                Log.e(TAG, "모든 세션 조회 실패: ${e.message}", e)
                emptyList()
            }
        }

        /**
         * 세션 삭제
         */
        @Suppress("TooGenericExceptionCaught")
        override suspend fun deleteChatSession(sessionId: String) {
            try {
                Log.d(TAG, "세션 삭제: $sessionId")
                chatSessionDao.deleteSessionById(sessionId)
                Log.d(TAG, "세션 삭제 완료: $sessionId")
            } catch (e: Exception) {
                Log.e(TAG, "세션 삭제 실패: ${e.message}", e)
                throw e
            }
        }

        companion object {
            private const val TAG = "ChatRepositoryImpl"
        }
    }
