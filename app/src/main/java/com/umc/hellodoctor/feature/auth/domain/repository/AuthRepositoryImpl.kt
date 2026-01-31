package com.umc.hellodoctor.feature.auth.domain.repository

import com.umc.hellodoctor.core.network.BaseResponse
import com.umc.hellodoctor.feature.auth.data.model.SocialLoginRequest
import com.umc.hellodoctor.feature.auth.data.model.SocialLoginResponse
import com.umc.hellodoctor.feature.auth.data.repository.AuthApi
import com.umc.hellodoctor.feature.auth.domain.model.SocialUser
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
) : AuthRepository {

    override suspend fun loginWithSocial(user: SocialUser): BaseResponse<SocialLoginResponse> {
        val request = SocialLoginRequest(
            idToken = user.idToken,
        )
        return authApi.socialLogin(request)
    }
}