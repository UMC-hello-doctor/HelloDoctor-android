package com.umc.hellodoctor.feature.auth.presentation

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TokenManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val prefs: SharedPreferences = context.getSharedPreferences("auth_tokens", Context.MODE_PRIVATE)

        // 🔑 Access Token (기존)
        fun getAccessToken(): String = prefs.getString("access_token", "") ?: ""

        fun saveAccessToken(token: String) = prefs.edit { putString("access_token", token) }

        // 🔄 Refresh Token (신규)
        fun getRefreshToken(): String = prefs.getString("refresh_token", "") ?: ""

        fun saveRefreshToken(token: String) = prefs.edit { putString("refresh_token", token) }

        // 🎯 Google ID Token (신규 - Google 로그인용)
        fun getGoogleIdToken(): String = prefs.getString("google_id_token", "") ?: ""

        fun saveGoogleIdToken(token: String) = prefs.edit { putString("google_id_token", token) }

        // 📱 로그인 후 전체 저장 (Response에서 한 번에)
        fun saveAuthTokens(
            accessToken: String? = null,
            refreshToken: String? = null,
        ) {
            prefs.edit {
                accessToken?.let { putString("access_token", it) }
                refreshToken?.let { putString("refresh_token", it) }
            }
        }

        // 🧹 로그아웃 (전체 삭제)
        fun clearAllTokens() {
            prefs.edit { clear() }
        }

        // ✅ 토큰 유효성 검사
        fun isLoggedIn(): Boolean = getAccessToken().isNotEmpty()

        // 📊 디버깅 (로그용)
        fun debugLog() {
            Log.d(
                "TokenManager",
                """
                Access: ${getAccessToken().take(TOKEN_DEBUG_PREVIEW_LENGTH)}...
                Refresh: ${getRefreshToken().take(TOKEN_DEBUG_PREVIEW_LENGTH)}...
                GoogleID: ${getGoogleIdToken().take(TOKEN_DEBUG_PREVIEW_LENGTH)}...
                """.trimIndent(),
            )
        }

        companion object {
            private const val TOKEN_DEBUG_PREVIEW_LENGTH = 20
        }
    }
