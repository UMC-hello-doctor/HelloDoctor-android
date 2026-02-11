package com.umc.hellodoctor.feature.userinfo.domain.repository

import com.umc.hellodoctor.feature.userinfo.data.remote.UserProfileApi
import com.umc.hellodoctor.feature.userinfo.data.remote.model.MyProfileResponse
import com.umc.hellodoctor.feature.userinfo.data.remote.model.ProfileCreateRequest
import com.umc.hellodoctor.feature.userinfo.data.remote.model.ProfileCreateResult
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val api: UserProfileApi
) : UserProfileRepository {

    override suspend fun createProfile(token: String, body: ProfileCreateRequest): ProfileCreateResult {
        val res = api.createProfile(bearerToken = "Bearer $token", body = body)
        if (!res.success || res.result == null) error(res.message)
        return res.result
    }

    override suspend fun getMyProfile(token: String): MyProfileResponse {
        val res = api.getMyProfile(bearerToken = "Bearer $token")
        if (!res.success || res.result == null) error(res.message)
        return res.result
    }
}
