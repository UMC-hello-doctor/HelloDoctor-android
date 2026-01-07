package com.umc.hellodoctor.feature.auth.domain.model

data class SocialUser(
    val provider: SocialProvider,
    val idToken: String?,      // Google/Apple ID 토큰 등
    val accessToken: String?,  // Kakao access token 등
    val email: String?,
    val name: String?
)