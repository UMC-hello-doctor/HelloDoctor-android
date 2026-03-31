package com.umc.hellodoctor.core.util

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Context.toast(
    message: CharSequence,
    isLong: Boolean = false,
) {
    Toast.makeText(
        this,
        message,
        if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT,
    ).show()
}

// Fragment 확장
fun Fragment.toast(
    message: CharSequence,
    isLong: Boolean = false,
) {
    requireContext().toast(message, isLong)
}

fun showToast(
    context: Context,
    message: CharSequence,
    isLong: Boolean = false,
) {
    Toast.makeText(
        context,
        message,
        if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT,
    ).show()
}
