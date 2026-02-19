package com.umc.hellodoctor.feature.userinfo.data.repository

import android.util.Log
import com.umc.hellodoctor.feature.userinfo.data.api.UserInfoApi
import com.umc.hellodoctor.feature.userinfo.data.remote.model.MyProfileResponse
import com.umc.hellodoctor.feature.userinfo.data.remote.model.ProfileCreateRequest
import com.umc.hellodoctor.feature.userinfo.data.remote.model.ProfileCreateResult
import com.umc.hellodoctor.feature.userinfo.domain.repository.UserInfoRepository
import javax.inject.Inject

class UserInfoRepositoryImpl @Inject constructor(
    private val userInfoApi: UserInfoApi
) : UserInfoRepository {

    private val TAG = "UserInfoRepository"

    override suspend fun createProfile(request: ProfileCreateRequest): Result<ProfileCreateResult> {
        return try {
            Log.d(TAG, "Creating profile: $request")
            val response = userInfoApi.createProfile(request)

            if (response.success && response.result != null) {
                Log.d(TAG, "Profile created successfully: ${response.result}")
                Result.success(response.result)
            } else {
                Log.e(TAG, "Profile creation failed: ${response.code}-${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Profile creation error", e)
            Result.failure(e)
        }
    }

    override suspend fun getMyProfile(): Result<MyProfileResponse> {
        return try {
            Log.d(TAG, "Getting my profile")
            val response = userInfoApi.getMyProfile()

            if (response.success && response.result != null) {
                Log.d(TAG, "Profile fetched successfully: ${response.result}")
                Result.success(response.result)
            } else {
                Log.e(TAG, "Profile fetch failed: ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Profile fetch error", e)
            Result.failure(e)
        }
    }
}

