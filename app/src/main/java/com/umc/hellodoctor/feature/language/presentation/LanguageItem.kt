package com.umc.hellodoctor.feature.language.presentation

data class LanguageItem(
    val label: String,
    val iconRes: Int
) {
    override fun toString(): String = label // AutoComplete이 내부적으로 문자열 표시할 때 사용
}