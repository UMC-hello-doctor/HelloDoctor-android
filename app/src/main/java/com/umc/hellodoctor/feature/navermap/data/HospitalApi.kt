package com.umc.hellodoctor.feature.navermap.data

import com.umc.hellodoctor.core.network.BaseResponse
import com.umc.hellodoctor.feature.navermap.data.HospitalItem
import retrofit2.http.GET
import retrofit2.http.Query

interface HospitalApi {
    @GET("v1/places/search")  // 실제 엔드포인트
    suspend fun getNearbyHospitals(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("dept") department: String = "내과"
    ):  BaseResponse<List<HospitalItem>>
}