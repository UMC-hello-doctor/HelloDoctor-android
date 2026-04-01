package com.umc.hellodoctor.feature.auth.domain.model

sealed class SocialSignInResult {
    data class Success(val user: SocialUser) : SocialSignInResult()

    object Canceled : SocialSignInResult()

    data class Error(val throwable: Throwable) : SocialSignInResult()
}
