package com.umc.hellodoctor.feature.userinfo.domain.repository

import com.umc.hellodoctor.feature.userinfo.data.model.MyProfileResponse
import com.umc.hellodoctor.feature.userinfo.data.model.ProfileCreateRequest
import com.umc.hellodoctor.feature.userinfo.data.model.ProfileCreateResult

interface UserInfoRepository {
    suspend fun createProfile(request: ProfileCreateRequest): Result<ProfileCreateResult>

    suspend fun getMyProfile(): Result<MyProfileResponse>
}
