package com.umc.hellodoctor.feature.auth.domain.repository

import com.umc.hellodoctor.feature.auth.domain.model.SocialSignInResult

interface SocialAuthService {
    suspend fun signIn(): SocialSignInResult

    suspend fun signOut()
}
