package com.umc.hellodoctor.feature.chat.data.ai

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit service for Google Gemini REST API
 * API Endpoint: POST /v1beta/models/{model}:generateContent?key={API_KEY}
 * Models: gemini-pro, gemini-1.5-pro, gemini-2.0-flash
 */
interface AiService {
    @POST("/v1beta/models/gemini-2.5-flash-lite:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateRequest,
    ): GeminiGenerateResponse
}
