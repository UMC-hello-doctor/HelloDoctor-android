package com.umc.hellodoctor.feature.drug.data.repository

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.umc.hellodoctor.feature.drug.data.ocr.PrescriptionTextParser
import com.umc.hellodoctor.feature.drug.domain.model.OcrResult
import com.umc.hellodoctor.feature.drug.domain.repository.OcrRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume

class OcrRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : OcrRepository {
        private val recognizer by lazy {
            TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
        }
        private val parser = PrescriptionTextParser()

        override suspend fun recognizeText(imageUri: Uri): Result<OcrResult> =
            suspendCancellableCoroutine { cont ->
                try {
                    val image = InputImage.fromFilePath(context, imageUri)
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            cont.resume(Result.success(parser.parse(visionText.text)))
                        }
                        .addOnFailureListener { exception ->
                            cont.resume(Result.failure(exception))
                        }
                } catch (e: IOException) {
                    cont.resume(Result.failure(e))
                }
            }
    }
