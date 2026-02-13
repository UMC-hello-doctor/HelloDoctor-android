package com.umc.hellodoctor.feature.auth.data.repository

import com.umc.hellodoctor.core.network.BaseResponse
import com.umc.hellodoctor.feature.auth.data.model.SocialLoginRequest
import com.umc.hellodoctor.feature.auth.data.model.SocialLoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("v1/auth/google")
    suspend fun socialLogin(
        @Body body: SocialLoginRequest
    ): BaseResponse<SocialLoginResponse>
}