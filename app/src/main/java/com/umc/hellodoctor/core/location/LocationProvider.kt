package com.umc.hellodoctor.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * FusedLocationProviderClient를 감싸서
 * - 현재 위치 1회 조회 (suspend)
 * - 연속 위치 업데이트 (Flow)
 * 를 코루틴 방식으로 제공하는 위치 헬퍼 클래스.
 */
@Singleton
class LocationProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    /**
     * 기기의 현재 위치를 한 번만 가져오는 suspend 함수.
     *
     * 사용 전제:
     * - 호출하는 쪽에서 ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION 권한을 이미 허용한 상태여야 한다.
     *
     * 성공 시:
     * - 마지막으로 알려진 Location 객체를 반환한다.
     *
     * 실패/예외 시:
     * - 최근 위치 캐시가 없으면 IllegalStateException("No last location") 발생
     * - 위치 권한이 없거나 보안 문제 시 SecurityException 발생
     * - FusedLocationProviderClient 내부 에러 시 해당 예외를 그대로 전달
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocationOnce(): Location =
        suspendCancellableCoroutine { cont ->
            try {
                client.lastLocation
                    .addOnSuccessListener { location ->
                        if (!cont.isActive) return@addOnSuccessListener

                        if (location != null) {
                            cont.resume(location)
                        } else {
                            cont.resumeWithException(
                                IllegalStateException("No last location")
                            )
                        }
                    }
                    .addOnFailureListener { e ->
                        if (cont.isActive) cont.resumeWithException(e)
                    }
            } catch (e: SecurityException) {
                if (cont.isActive) cont.resumeWithException(e)
            }
        }

    /**
     * 지정된 주기로 연속 위치 업데이트를 방출하는 Flow.
     *
     * 사용 전제:
     * - 호출하는 쪽에서 위치 권한을 허용한 상태여야 한다.
     * - Flow를 collect하는 동안만 requestLocationUpdates가 유지되고,
     *   collect가 취소되면 자동으로 removeLocationUpdates가 호출된다.
     *
     * @param intervalMillis 위치 갱신 간격(밀리초). 기본 3초.
     * @param minDistanceMeters 최소 이동 거리(미터). 이 거리 이상 이동했을 때만 업데이트를 받을 때 사용.
     *
     * @return Location 객체를 계속 방출하는 Flow
     */
    @SuppressLint("MissingPermission")
    fun locationUpdatesFlow(
        intervalMillis: Long = 3000L,
        minDistanceMeters: Float = 0f
    ): Flow<Location> = callbackFlow {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            intervalMillis
        ).setMinUpdateDistanceMeters(minDistanceMeters)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it).isSuccess }
            }
        }

        try {
            client.requestLocationUpdates(
                request,
                callback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // 권한 문제 등으로 등록 실패 시 Flow를 에러로 닫음
            close(e)
        }

        // collect가 취소되거나 scope가 종료될 때 호출되어 콜백 정리
        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }
}