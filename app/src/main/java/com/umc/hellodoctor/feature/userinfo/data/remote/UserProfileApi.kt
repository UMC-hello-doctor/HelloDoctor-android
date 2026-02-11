package com.umc.hellodoctor.feature.userinfo.data.remote

import com.umc.hellodoctor.core.network.model.ApiResponse
import com.umc.hellodoctor.feature.userinfo.data.remote.model.ProfileCreateRequest
import com.umc.hellodoctor.feature.userinfo.data.remote.model.ProfileCreateResult
import com.umc.hellodoctor.feature.userinfo.data.remote.model.MyProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface UserProfileApi {

    @POST("/api/users/profiles")
    suspend fun createProfile(
        @Header("Authorization") bearerToken: String,
        @Body body: ProfileCreateRequest
    ): ApiResponse<ProfileCreateResult>

    @GET("/api/users/profiles/me")
    suspend fun getMyProfile(
        @Header("Authorization") bearerToken: String
    ): ApiResponse<MyProfileResponse>
}
