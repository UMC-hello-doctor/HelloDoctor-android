package com.umc.hellodoctor.feature.auth.domain.repository

import com.umc.hellodoctor.core.network.BaseResponse
import com.umc.hellodoctor.feature.auth.data.model.SocialLoginResponse
import com.umc.hellodoctor.feature.auth.domain.model.SocialUser

interface AuthRepository {
    suspend fun loginWithSocial(user: SocialUser): BaseResponse<SocialLoginResponse>
}
