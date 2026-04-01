package com.umc.hellodoctor.feature.navermap.data

data class HospitalItem(
    val id: String,
    val name: String,
    val address: String,
    val tel: String,
    val distance: Double,
    val latitude: Double,
    val longitude: Double,
    val businessHours: String,
)
