package com.umc.hellodoctor.feature.drug.data.api

import com.umc.hellodoctor.core.network.BaseResponse
import com.umc.hellodoctor.feature.drug.data.model.MedicineSearchItem
import retrofit2.http.GET
import retrofit2.http.Query

interface MedicineApi {
    @GET("v1/medicines/search")
    suspend fun searchMedicines(
        @Query("name") keyword: String
    ): BaseResponse<List<MedicineSearchItem>>
}

