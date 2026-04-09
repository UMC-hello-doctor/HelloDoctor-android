package com.umc.hellodoctor.feature.auth.presentation

import androidx.annotation.DrawableRes

data class OnboardingPage(
    val title: String,
    val highlight: String? = null,
    val desc: String,
    @DrawableRes val image1: Int,
    @DrawableRes val image2: Int? = null,
    val showStartButton: Boolean = false
)