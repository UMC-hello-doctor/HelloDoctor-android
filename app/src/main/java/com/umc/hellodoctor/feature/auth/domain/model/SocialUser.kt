package com.umc.hellodoctor.feature.auth.domain.model

data class SocialUser(
    val provider: SocialProvider,
    // Google/Apple ID 토큰 등
    val idToken: String?,
    // Kakao access token 등
    val accessToken: String?,
    val email: String?,
    val name: String?,
)
