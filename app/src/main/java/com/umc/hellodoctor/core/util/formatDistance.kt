package com.umc.hellodoctor.core.util

fun formatDistance(distance: Double): String {
    return when {
        distance >= 1000 -> {
            val km = distance / 1000.0
            "%.1f km".format(km)  // 1.2 km, 3.0 km 등
        }
        else -> {
            "${distance.toInt()} m"  // 999 m
        }
    }
}
