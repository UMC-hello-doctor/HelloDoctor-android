package com.umc.hellodoctor.core.network.model

data class ApiResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val result: T?
)
