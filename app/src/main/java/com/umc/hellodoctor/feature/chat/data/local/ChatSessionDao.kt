package com.umc.hellodoctor.feature.chat.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Room DAO - ChatSession CRUD
 */
@Dao
interface ChatSessionDao {

    /**
     * 채팅 세션 저장 (이미 존재하면 덮어쓰기)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    /**
     * 채팅 세션 업데이트
     */
    @Update
    suspend fun updateSession(session: ChatSessionEntity)

    /**
     * 세션 ID로 조회
     */
    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): ChatSessionEntity?

    /**
     * 모든 세션 조회 (최신순)
     */
    @Query("SELECT * FROM chat_sessions ORDER BY createdAt DESC")
    suspend fun getAllSessions(): List<ChatSessionEntity>

    /**
     * 세션 삭제
     */
    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: String)

    /**
     * 모든 세션 삭제
     */
    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAllSessions()

    /**
     * 세션 개수 조회
     */
    @Query("SELECT COUNT(*) FROM chat_sessions")
    suspend fun getSessionCount(): Int

    /**
     * 특정 날짜 이전 세션 삭제
     */
    @Query("DELETE FROM chat_sessions WHERE createdAt < :timestamp")
    suspend fun deleteSessionsOlderThan(timestamp: Long)
}

