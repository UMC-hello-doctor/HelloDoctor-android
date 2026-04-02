package com.umc.hellodoctor.feature.drug.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.umc.hellodoctor.feature.drug.data.ocr.PrescriptionTextParser
import com.umc.hellodoctor.feature.drug.domain.model.OcrResult
import com.umc.hellodoctor.feature.drug.domain.repository.OcrRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume

class OcrRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext @Suppress("UnusedPrivateProperty") private val context: Context,
    ) : OcrRepository {

    companion object {
        private const val TAG = "OcrRepository"
        private const val ROTATION_90 = 90
        private const val ROTATION_180 = 180
        private const val ROTATION_270 = 270
        private const val CONTRAST_SCALE = 1.5f
        private const val CONTRAST_TRANSLATE = -50f
        private const val OCR_TIMEOUT_MS = 5000L
    }

    private val recognizer by lazy {
        TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    }

    private val parser = PrescriptionTextParser()

    @Suppress("TooGenericExceptionCaught", "LongMethod")
    override suspend fun recognizeText(imageUri: Uri): Result<OcrResult> {
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "OCR 인식 시작: $imageUri")

        return withTimeoutOrNull(OCR_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                try {
                    // 파일 경로 추출
                    val filePath = imageUri.path ?: throw IOException("유효하지 않은 이미지 파일입니다")

                    // EXIF 회전 정보 확인
                    val exif = ExifInterface(filePath)
                    val rotationDegrees = when (exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> ROTATION_90
                        ExifInterface.ORIENTATION_ROTATE_180 -> ROTATION_180
                        ExifInterface.ORIENTATION_ROTATE_270 -> ROTATION_270
                        else -> 0
                    }

                    // Bitmap 디코딩
                    val originalBitmap = BitmapFactory.decodeFile(filePath)
                        ?: throw IOException("이미지 파일을 읽을 수 없습니다\n형식 또는 용량 확인 필요")

                    // 전처리: 그레이스케일 + 대비 강화
                    val preprocessedBitmap = preprocessBitmap(originalBitmap)

                    // InputImage 생성
                    val image = InputImage.fromBitmap(preprocessedBitmap, rotationDegrees)

                    // OCR 처리
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            preprocessedBitmap.recycle()
                            originalBitmap.recycle()

                            // 좌표 기준 텍스트 정렬
                            val sortedText = sortTextBlocksByCoordinates(visionText)
                            val result = parser.parse(sortedText)

                            val elapsedTime = System.currentTimeMillis() - startTime
                            Log.d(TAG, "OCR 성공 (${elapsedTime}ms): 약명 ${result.recognizedMedicineNames.size}개 인식")

                            cont.resume(Result.success(result))
                        }
                        .addOnFailureListener { exception ->
                            preprocessedBitmap.recycle()
                            originalBitmap.recycle()

                            val elapsedTime = System.currentTimeMillis() - startTime
                            Log.e(TAG, "OCR 실패 (${elapsedTime}ms): ${exception.message}", exception)

                            val errorMsg = exception.message
                                ?: "약명을 인식할 수 없습니다\n각도를 맞춰 다시 촬영해주세요"

                            cont.resume(
                                Result.failure(
                                    Exception(
                                        if (errorMsg.contains("시간 초과")) "분석 시간 초과\n다시 촬영해주세요"
                                        else errorMsg
                                    )
                                )
                            )
                        }
                } catch (e: IllegalArgumentException) {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    Log.e(TAG, "이미지 처리 오류 (${elapsedTime}ms): ${e.message}", e)
                    cont.resume(Result.failure(Exception("이미지 처리 중 오류가 발생했습니다")))
                } catch (e: IOException) {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    Log.e(TAG, "파일 읽기 오류 (${elapsedTime}ms): ${e.message}", e)
                    cont.resume(Result.failure(e))
                } catch (e: RuntimeException) {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    Log.e(TAG, "예상치 못한 오류 (${elapsedTime}ms): ${e.message}", e)
                    cont.resume(Result.failure(Exception("예상치 못한 오류가 발생했습니다\n다시 촬영해주세요")))
                }
            }
        } ?: run {
            val elapsedTime = System.currentTimeMillis() - startTime
            Log.w(TAG, "타임아웃 (${elapsedTime}ms 초과)")
            Result.failure(Exception("OCR 분석 시간 초과 (5초 이상)"))
        }
    }

    /**
     * 좌표 기반 텍스트 정렬
     * 블록 -> 라인 -> 요소 순서로 읽어서 정확도 향상
     */
    private fun sortTextBlocksByCoordinates(visionText: Text): String {
        val allLines = mutableListOf<Text.Line>()

        // 모든 블록에서 라인 추출
        for (block in visionText.textBlocks) {
            allLines.addAll(block.lines)
        }

        if (allLines.isEmpty()) return visionText.text

        // Y좌표 기준 그룹화 (tolerance 10)
        val lineGroups = mutableListOf<MutableList<Text.Line>>()
        val yTolerance = 10

        for (line in allLines) {
            val boundingBox = line.boundingBox ?: continue
            val lineTop = boundingBox.top

            val existingGroup = lineGroups.find { group ->
                val groupTop = group[0].boundingBox?.top ?: 0
                kotlin.math.abs(lineTop - groupTop) <= yTolerance
            }

            if (existingGroup != null) {
                existingGroup.add(line)
            } else {
                lineGroups.add(mutableListOf(line))
            }
        }

        // 그룹별 X 좌표 정렬 후 문자열 결합
        val sortedGroups = lineGroups
            .sortedBy { group -> group[0].boundingBox?.top ?: 0 }
            .map { group ->
                group.sortedBy { it.boundingBox?.left ?: 0 }
                    .joinToString(" ") { it.text }
            }

        return sortedGroups.joinToString("\n")
    }

    /**
     * Bitmap 전처리: 그레이스케일 + 대비 강화
     */
    private fun preprocessBitmap(src: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()

        // 그레이스케일
        val grayScale = ColorMatrix()
        grayScale.setSaturation(0f)

        // 대비 강화
        val contrast = ColorMatrix(
            floatArrayOf(
                CONTRAST_SCALE, 0f, 0f, 0f, CONTRAST_TRANSLATE,
                0f, CONTRAST_SCALE, 0f, 0f, CONTRAST_TRANSLATE,
                0f, 0f, CONTRAST_SCALE, 0f, CONTRAST_TRANSLATE,
                0f, 0f, 0f, 1f, 0f
            )
        )
        grayScale.postConcat(contrast)

        paint.colorFilter = ColorMatrixColorFilter(grayScale)
        canvas.drawBitmap(src, 0f, 0f, paint)

        return output
    }
}