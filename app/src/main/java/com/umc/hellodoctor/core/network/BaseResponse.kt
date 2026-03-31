package com.umc.hellodoctor.core.network

data class BaseResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    // 제네릭으로 result 타입 유연하게
    val result: T,
)
