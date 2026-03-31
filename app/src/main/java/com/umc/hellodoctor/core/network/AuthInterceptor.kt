package com.umc.hellodoctor.core.network

import android.util.Log
import com.umc.hellodoctor.feature.auth.presentation.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor
    @Inject
    constructor(
        private val tokenManager: TokenManager,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            // 토큰 가져오기
            val accessToken = tokenManager.getAccessToken()
            Log.d("TAG", "intercept: accessToken 추가 $accessToken")
            // 헤더 추가
            val newRequest =
                originalRequest.newBuilder().apply {
                    if (accessToken.isNotEmpty()) {
                        addHeader("Authorization", "Bearer $accessToken")
                    }
                    // 공통 헤더 추가
                    addHeader("Content-Type", "application/json")
                    addHeader("Accept", "application/json")
                }.build()

            return chain.proceed(newRequest)
        }
    }
