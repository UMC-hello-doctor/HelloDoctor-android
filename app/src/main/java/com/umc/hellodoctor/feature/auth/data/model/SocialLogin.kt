package com.umc.hellodoctor.feature.auth.data.model

data class SocialLoginRequest(
    val idToken: String?,
)

data class SocialLoginResponse(
    val accessToken: String,
    val refreshToken: String?,
    val isNewUser: Boolean,
)
