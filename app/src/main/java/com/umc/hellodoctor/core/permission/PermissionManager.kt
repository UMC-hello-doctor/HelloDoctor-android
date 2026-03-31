package com.umc.hellodoctor.core.permission

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionManager(
    private val context: Context,
    caller: ActivityResultCaller,
) {
    interface Callback {
        fun onPermissionsResult(
            granted: List<String>,
            denied: List<String>,
        )
    }

    private var callback: Callback? = null

    private val multiplePermissionLauncher =
        caller.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { result ->
            val granted = result.filterValues { it }.keys.toList()
            val denied = result.filterValues { !it }.keys.toList()
            callback?.onPermissionsResult(granted, denied)
        }

    // ===== 공통 체크 =====

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission,
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasLocationPermission(): Boolean = PermissionConstants.LOCATION_PERMISSIONS.any { hasPermission(it) }

    fun hasCameraPermission(): Boolean = PermissionConstants.CAMERA_PERMISSIONS.any { hasPermission(it) }

    fun hasNotificationPermission(): Boolean = PermissionConstants.NOTIFICATION_PERMISSIONS.all { hasPermission(it) }

    fun canScheduleExactAlarms(): Boolean {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager?.canScheduleExactAlarms() == true
        } else {
            true
        }
    }
    // ===== 앱 실행 시 기본 권한 요청 =====

    /**
     * 앱 공통으로 필요하다고 보는 권한들을 한 번에 요청.
     * (알림 + 위치 + 카메라 + 정확 알람)
     */
    fun requestInitialPermissions(callback: Callback) {
        val permissionGroups =
            listOf(
                PermissionConstants.NOTIFICATION_PERMISSIONS,
                PermissionConstants.LOCATION_PERMISSIONS,
                PermissionConstants.CAMERA_PERMISSIONS,
            )

        requestPermissions(
            permissionGroups = permissionGroups,
            requestExactAlarm = true,
            callback = callback,
        )
    }
    // ===== 동적 요청 (Constants 활용) =====

    /**
     * permissionGroups에는 PermissionConstants의 배열들을 원하는 조합으로 넣으면 됩니다.
     *
     * 예:
     * requestPermissions(
     *   permissionGroups = listOf(
     *     PermissionConstants.NOTIFICATION_PERMISSIONS,
     *     PermissionConstants.LOCATION_PERMISSIONS
     *   ),
     *   requestExactAlarm = true
     * )
     */
    fun requestPermissions(
        permissionGroups: List<Array<String>>,
        requestExactAlarm: Boolean = false,
        callback: Callback,
    ) {
        this.callback = callback

        val runtimePermissions = mutableSetOf<String>()
        var needExactAlarmRequest = false

        // 1) 전달된 배열들(LOCATION/CAMERA/NOTIFICATION)을 평탄화해서 아직 허용 안 된 권한만 수집
        permissionGroups
            .flatMap { it.asIterable() } // Array<String> -> Iterable<String>로 변환 후 flatten 효과
            .forEach { permission ->
                if (isRuntimePermission(permission) && !hasPermission(permission)) {
                    runtimePermissions += permission
                }
            }

        // 2) 정확 알람
        if (requestExactAlarm && !canScheduleExactAlarms()) {
            needExactAlarmRequest = true
        }

        // 3) 런타임 권한 요청
        if (runtimePermissions.isNotEmpty()) {
            multiplePermissionLauncher.launch(runtimePermissions.toTypedArray())
        } else {
            callback.onPermissionsResult(emptyList(), emptyList())
        }

        // 4) 정확 알람 필요하면 설정 화면으로 이동
        if (needExactAlarmRequest) {
            requestExactAlarmPermissionInternal()
        }
    }

    private fun isRuntimePermission(permission: String): Boolean {
        // 현재 사용하는 권한만 고려
        if (permission == Manifest.permission.POST_NOTIFICATIONS &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            return false
        }
        return true
    }

    private fun requestExactAlarmPermissionInternal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent =
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            context.startActivity(intent)
        }
    }
}
