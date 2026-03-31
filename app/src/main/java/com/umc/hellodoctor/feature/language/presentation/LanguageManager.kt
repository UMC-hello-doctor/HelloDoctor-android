package com.umc.hellodoctor.feature.language.presentation

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.Locale

class LanguageManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("language_settings", Context.MODE_PRIVATE)

    companion object {
        private const val LANGUAGE_KEY = "selected_language"
        private const val DEFAULT_LANGUAGE = "영어 (English)"
    }

    fun saveLanguage(language: String) {
        prefs.edit {
            putString(LANGUAGE_KEY, language)
        }
        applyLanguage()
    }

    fun getLanguage(): String {
        return prefs.getString(LANGUAGE_KEY, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun getLanguageCode(): String {
        return when (getLanguage()) {
            "한국어" -> "ko"
            "영어 (English)" -> "en"
            "일본어 (日本語)" -> "ja"
            "중국어 (中文)" -> "zh"
            "베트남어 (tiếng Việt)" -> "vi"
            else -> "ko"
        }
    }

    fun applyLanguage() {
        val locale = Locale(getLanguageCode())
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
