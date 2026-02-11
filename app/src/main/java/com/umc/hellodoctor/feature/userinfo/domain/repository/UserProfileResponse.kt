package com.umc.hellodoctor.feature.userinfo.domain.repository

import com.umc.hellodoctor.feature.userinfo.data.remote.model.MyProfileResponse
import com.umc.hellodoctor.feature.userinfo.data.remote.model.ProfileCreateRequest
import com.umc.hellodoctor.feature.userinfo.data.remote.model.ProfileCreateResult

interface UserProfileRepository {
    suspend fun createProfile(token: String, body: ProfileCreateRequest): ProfileCreateResult
    suspend fun getMyProfile(token: String): MyProfileResponse
}
