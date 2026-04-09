package com.umc.hellodoctor.core.util

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan

private const val HIGHLIGHT_COLOR = "#1852FF"

fun makeHighlightedTitle(title: String, highlight: String?): CharSequence {
    if (highlight.isNullOrBlank()) return title

    val spannable = SpannableString(title)
    val start = title.indexOf(highlight)
    if (start >= 0) {
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor(HIGHLIGHT_COLOR)),
            start,
            start + highlight.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return spannable
}
