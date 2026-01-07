package com.umc.hellodoctor.feature.auth.data.repository

import com.umc.hellodoctor.feature.auth.data.model.SocialLoginRequest
import com.umc.hellodoctor.feature.auth.data.model.SocialLoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("")
    suspend fun socialLogin(
        @Body body: SocialLoginRequest
    ): SocialLoginResponse
}