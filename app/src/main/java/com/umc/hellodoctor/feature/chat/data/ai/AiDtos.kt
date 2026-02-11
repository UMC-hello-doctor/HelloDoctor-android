package com.umc.hellodoctor.feature.chat.data.ai

import com.google.gson.annotations.SerializedName

/**
 * DTOs for calling Google Gemini API via REST
 * API Endpoint: POST /v1beta/models/{model}:generateContent?key={API_KEY}
 * Model: gemini-pro, gemini-1.5-pro, gemini-2.0-flash
 */

// ============= Request DTOs =============

data class GeminiGenerateRequest(
    @SerializedName("contents")
    val contents: List<Content>,
    @SerializedName("generationConfig")
    val generationConfig: GenerationConfig? = null,
    @SerializedName("safetySettings")
    val safetySettings: List<SafetySetting>? = null,
    @SerializedName("systemInstruction")
    val systemInstruction: SystemInstruction? = null
)

data class Content(
    @SerializedName("role")
    val role: String, // "user" or "model"
    @SerializedName("parts")
    val parts: List<Part>
)

data class Part(
    @SerializedName("text")
    val text: String
)

data class GenerationConfig(
    @SerializedName("temperature")
    val temperature: Double = 0.7,
    @SerializedName("topP")
    val topP: Double = 0.95,
    @SerializedName("topK")
    val topK: Int = 40,
    @SerializedName("maxOutputTokens")
    val maxOutputTokens: Int = 1024,
    @SerializedName("stopSequences")
    val stopSequences: List<String>? = null
)

data class SafetySetting(
    @SerializedName("category")
    val category: String, // "HARM_CATEGORY_SEXUALLY_EXPLICIT", "HARM_CATEGORY_HATE_SPEECH", etc.
    @SerializedName("threshold")
    val threshold: String // "BLOCK_NONE", "BLOCK_ONLY_HIGH", "BLOCK_MEDIUM_AND_ABOVE", "BLOCK_LOW_AND_ABOVE"
)

data class SystemInstruction(
    @SerializedName("parts")
    val parts: List<Part>
)

// ============= Response DTOs =============

data class GeminiGenerateResponse(
    @SerializedName("candidates")
    val candidates: List<CandidateResponse>? = null,
    @SerializedName("promptFeedback")
    val promptFeedback: PromptFeedback? = null,
    @SerializedName("usageMetadata")
    val usageMetadata: UsageMetadata? = null
)

data class CandidateResponse(
    @SerializedName("content")
    val content: Content,
    @SerializedName("finishReason")
    val finishReason: String? = null, // "STOP", "MAX_TOKENS", "SAFETY", "RECITATION", "OTHER"
    @SerializedName("safetyRatings")
    val safetyRatings: List<SafetyRatingResponse>? = null,
    @SerializedName("index")
    val index: Int? = null
)

data class SafetyRatingResponse(
    @SerializedName("category")
    val category: String,
    @SerializedName("probability")
    val probability: String, // "NEGLIGIBLE", "LOW", "MEDIUM", "HIGH"
    @SerializedName("blocked")
    val blocked: Boolean? = false
)

data class PromptFeedback(
    @SerializedName("safetyRatings")
    val safetyRatings: List<SafetyRatingResponse>? = null,
    @SerializedName("blockReason")
    val blockReason: String? = null // "SAFETY", "OTHER"
)

data class UsageMetadata(
    @SerializedName("promptTokenCount")
    val promptTokenCount: Int? = null,
    @SerializedName("candidatesTokenCount")
    val candidatesTokenCount: Int? = null,
    @SerializedName("totalTokenCount")
    val totalTokenCount: Int? = null
)
