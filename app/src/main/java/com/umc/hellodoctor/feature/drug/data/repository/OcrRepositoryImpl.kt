package com.umc.hellodoctor.feature.drug.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
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

        @Suppress("TooGenericExceptionCaught")
        override suspend fun recognizeText(imageUri: Uri): Result<OcrResult> =
            withTimeoutOrNull(OCR_TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    try {
                        // Extract file path from URI
                        val filePath = imageUri.path ?: throw IOException("Invalid URI: $imageUri")

                        // Read EXIF rotation information
                        val exif = ExifInterface(filePath)
                        val rotationDegrees =
                            when (
                                exif.getAttributeInt(
                                    ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_NORMAL,
                                )
                            ) {
                                ExifInterface.ORIENTATION_ROTATE_90 -> ROTATION_90
                                ExifInterface.ORIENTATION_ROTATE_180 -> ROTATION_180
                                ExifInterface.ORIENTATION_ROTATE_270 -> ROTATION_270
                                else -> 0
                            }

                        // Decode bitmap from file
                        val originalBitmap =
                            BitmapFactory.decodeFile(filePath)
                                ?: throw IOException("Failed to decode image at $filePath")

                        // Preprocess bitmap (grayscale + contrast enhancement)
                        val preprocessedBitmap = preprocessBitmap(originalBitmap)

                        // Create InputImage with rotation information
                        val image = InputImage.fromBitmap(preprocessedBitmap, rotationDegrees)

                        recognizer.process(image)
                            .addOnSuccessListener { visionText ->
                                preprocessedBitmap.recycle()
                                originalBitmap.recycle()
                                cont.resume(Result.success(parser.parse(visionText.text)))
                            }
                            .addOnFailureListener { exception ->
                                preprocessedBitmap.recycle()
                                originalBitmap.recycle()
                                cont.resume(Result.failure(exception))
                            }
                    } catch (e: IllegalArgumentException) {
                        cont.resume(Result.failure(e))
                    } catch (e: IOException) {
                        cont.resume(Result.failure(e))
                    } catch (e: RuntimeException) {
                        cont.resume(Result.failure(e))
                    }
                }
            } ?: Result.failure(Exception("OCR 분석 시간 초과 (5초 이상)"))

        /**
         * Preprocess bitmap: convert to grayscale and enhance contrast
         * This improves OCR recognition accuracy for low-quality or unevenly-lit prescriptions
         */
        private fun preprocessBitmap(src: Bitmap): Bitmap {
            val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint()

            // Convert to grayscale
            val grayScale = ColorMatrix()
            grayScale.setSaturation(0f)

            // Enhance contrast: scale=1.5, translate=-50
            val contrast =
                ColorMatrix(
                    floatArrayOf(
                        CONTRAST_SCALE, 0f, 0f, 0f, CONTRAST_TRANSLATE,
                        0f, CONTRAST_SCALE, 0f, 0f, CONTRAST_TRANSLATE,
                        0f, 0f, CONTRAST_SCALE, 0f, CONTRAST_TRANSLATE,
                        0f, 0f, 0f, 1f, 0f,
                    ),
                )

            grayScale.postConcat(contrast)
            paint.colorFilter = ColorMatrixColorFilter(grayScale)
            canvas.drawBitmap(src, 0f, 0f, paint)

            return output
        }
    }
