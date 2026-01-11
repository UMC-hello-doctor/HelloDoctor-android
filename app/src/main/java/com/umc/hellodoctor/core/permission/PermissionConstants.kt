package com.umc.hellodoctor.core.permission

import android.Manifest
import android.os.Build

object PermissionConstants {

    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val CAMERA_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA
    )

    val NOTIFICATION_PERMISSIONS: Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }
}