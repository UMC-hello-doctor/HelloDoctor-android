package com.umc.hellodoctor.core.location

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.roundToInt

/**
 * 기기 방향(북을 0도로 한 0~360도 방위각)을 Flow<Float>로 제공하는 Provider.
 *
 * - 가능하면 TYPE_ROTATION_VECTOR를 우선 사용해서 방향을 계산한다.
 * - 없으면 ACCELEROMETER + MAGNETIC_FIELD 조합을 사용해 방위각을 계산한다.
 *
 * 사용 전제:
 * - 호출하는 쪽에서 센서 사용을 허용한 상태여야 한다.
 * - bearingFlow를 collect하는 동안에만 센서 리스너를 등록하고,
 *   collect가 취소되면 자동으로 센서 리스너를 해제한다.
 */
class BearingProvider(
    context: Context,
) {
    /** 센서 접근을 위한 SensorManager */
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    /** 기기 회전을 종합적으로 제공하는 Rotation Vector 센서 (있으면 우선 사용) */
    private val rotationVectorSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    /** 가속도 센서 (Rotation Vector가 없을 때 자기 센서와 함께 사용) */
    private val accelerometerSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    /** 자기장 센서 (Rotation Vector가 없을 때 가속도 센서와 함께 사용) */
    private val magneticFieldSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    /** 가속도 센서 값을 임시로 저장하는 배열 */
    private val accelValues = FloatArray(3)

    /** 자기장 센서 값을 임시로 저장하는 배열 */
    private val magnetValues = FloatArray(3)

    /**
     * 북을 0도로 하는 방위각(bearing)을 0f~360f 범위로 연속 방출하는 Flow.
     *
     * - Rotation Vector가 있으면 그 값으로 azimuth(방위각)를 계산한다.
     * - 없으면 ACCELEROMETER + MAGNETIC_FIELD에서 회전 행렬을 구해 azimuth를 계산한다.[web:50][web:57]
     * - 값은 소수 첫째 자리에서 반올림하여 노이즈를 조금 줄인다.
     */
    val bearingFlow: Flow<Float> =
        callbackFlow {
            // SensorEventListener를 callbackFlow 스코프 안에서 구현
            val listener =
                object : SensorEventListener {
                    /**
                     * 센서 값이 변경될 때마다 호출되며,
                     * 여기에서 방위각(azimuth)을 계산해 Flow로 흘려보낸다.
                     */
                    override fun onSensorChanged(event: SensorEvent) {
                        val azimuthDeg: Float =
                            when (event.sensor.type) {
                                // Rotation Vector 센서가 있는 경우: 회전 행렬 → orientation → azimuth 계산
                                Sensor.TYPE_ROTATION_VECTOR -> {
                                    val rMat = FloatArray(9)
                                    SensorManager.getRotationMatrixFromVector(rMat, event.values)

                                    val orientation = FloatArray(3)
                                    SensorManager.getOrientation(rMat, orientation)
                                    // orientation[0] = azimuth (라디안) → 도로 변환 후 0~360 범위로 정규화
                                    ((Math.toDegrees(orientation[0].toDouble()) + 360) % 360).toFloat()
                                }

                                // 가속도/자기장 센서 조합으로 회전 행렬을 계산하는 경우[web:50]
                                Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_MAGNETIC_FIELD -> {
                                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                                        System.arraycopy(event.values, 0, accelValues, 0, accelValues.size)
                                    } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                                        System.arraycopy(event.values, 0, magnetValues, 0, magnetValues.size)
                                    }

                                    val rMat = FloatArray(9)
                                    val success =
                                        SensorManager.getRotationMatrix(
                                            rMat,
                                            null,
                                            accelValues,
                                            magnetValues,
                                        )
                                    if (!success) return

                                    val orientation = FloatArray(3)
                                    SensorManager.getOrientation(rMat, orientation)
                                    ((Math.toDegrees(orientation[0].toDouble()) + 360) % 360).toFloat()
                                }

                                // 그 외 센서 타입은 무시
                                else -> return
                            }

                        // 소수 첫째 자리까지 반올림해서 너무 자주 튀는 값을 완화
                        val rounded = ((azimuthDeg * 10).roundToInt() / 10f)

                        // callbackFlow의 채널로 값 전송
                        trySend(rounded)
                    }

                    override fun onAccuracyChanged(
                        sensor: Sensor?,
                        accuracy: Int,
                    ) {
                        // 정확도 변경 이벤트는 여기서는 별도 처리 없음
                    }
                }

            // 사용 가능한 센서를 기준으로 리스너를 등록한다.
            if (rotationVectorSensor != null) {
                // Rotation Vector 센서가 있으면 이 한 가지 센서만 사용
                sensorManager.registerListener(
                    listener,
                    rotationVectorSensor,
                    SensorManager.SENSOR_DELAY_GAME,
                )
            } else {
                // 없으면 가속도 + 자기장 센서를 함께 등록[web:50]
                accelerometerSensor?.let {
                    sensorManager.registerListener(
                        listener,
                        it,
                        SensorManager.SENSOR_DELAY_GAME,
                    )
                }
                magneticFieldSensor?.let {
                    sensorManager.registerListener(
                        listener,
                        it,
                        SensorManager.SENSOR_DELAY_GAME,
                    )
                }
            }

            /**
             * bearingFlow의 collect가 취소되거나 scope가 종료될 때 호출되어,
             * 센서 리스너를 해제해 리소스를 정리한다.
             */
            awaitClose {
                sensorManager.unregisterListener(listener)
            }
        }
}
