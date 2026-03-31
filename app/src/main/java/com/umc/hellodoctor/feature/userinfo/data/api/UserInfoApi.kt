package com.umc.hellodoctor.feature.userinfo.data.api

import com.umc.hellodoctor.core.network.BaseResponse
import com.umc.hellodoctor.feature.userinfo.data.remote.model.MyProfileResponse
import com.umc.hellodoctor.feature.userinfo.data.remote.model.ProfileCreateRequest
import com.umc.hellodoctor.feature.userinfo.data.remote.model.ProfileCreateResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserInfoApi {
    @POST("v1/users/profiles")
    suspend fun createProfile(
        @Body request: ProfileCreateRequest,
    ): BaseResponse<ProfileCreateResult>

    @GET("v1/users/profiles/me")
    suspend fun getMyProfile(): BaseResponse<MyProfileResponse>
}
