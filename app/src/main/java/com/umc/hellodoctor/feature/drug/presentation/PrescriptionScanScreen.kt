package com.umc.hellodoctor.feature.drug.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.umc.hellodoctor.feature.drug.domain.model.OcrResult
import java.io.File

@Composable
internal fun PrescriptionScanScreen(
    uiState: PrescriptionScanUiState,
    callbacks: PrescriptionScanCallbacks,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted -> hasCameraPermission = granted }
    val imageCaptureRef = remember { mutableStateOf<ImageCapture?>(null) }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        if (hasCameraPermission) {
            CameraPreviewContent(lifecycleOwner, imageCaptureRef, callbacks.onCaptureFailed)
        } else {
            NoCameraPermissionContent { permissionLauncher.launch(Manifest.permission.CAMERA) }
        }
        ScanStateOverlay(uiState, callbacks.onViewResult, callbacks.onRetake)
        IconButton(
            onClick = callbacks.onBack,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로가기",
                tint = Color.White,
            )
        }
        if (uiState is PrescriptionScanUiState.Idle || uiState is PrescriptionScanUiState.Error) {
            CaptureButton(
                hasCameraPermission = hasCameraPermission,
                imageCaptureRef = imageCaptureRef,
                context = context,
                onImageCaptured = callbacks.onImageCaptured,
                onCaptureFailed = callbacks.onCaptureFailed,
            )
        }
    }
}

@Composable
private fun CameraPreviewContent(
    lifecycleOwner: LifecycleOwner,
    imageCaptureRef: MutableState<ImageCapture?>,
    onCaptureFailed: (String) -> Unit,
) {
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).also { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener(
                    {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview =
                            Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                        val imageCapture =
                            ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                        imageCaptureRef.value = imageCapture
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture,
                            )
                        } catch (e: IllegalStateException) {
                            onCaptureFailed(e.message ?: "카메라 초기화 실패")
                        } catch (e: IllegalArgumentException) {
                            onCaptureFailed(e.message ?: "카메라 초기화 실패")
                        }
                    },
                    ContextCompat.getMainExecutor(ctx),
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun NoCameraPermissionContent(onRequestPermission: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "처방전을 촬영하려면 카메라 권한이 필요합니다.",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("권한 허용")
        }
    }
}

@Composable
private fun BoxScope.ScanStateOverlay(
    uiState: PrescriptionScanUiState,
    onViewResult: (OcrResult) -> Unit,
    onRetake: () -> Unit,
) {
    when (uiState) {
        is PrescriptionScanUiState.Loading -> LoadingOverlay()
        is PrescriptionScanUiState.Success -> SuccessOverlay(uiState.result, onViewResult, onRetake)
        is PrescriptionScanUiState.Error -> ErrorBanner(uiState.message)
        is PrescriptionScanUiState.Idle -> Unit
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "처방전을 분석 중입니다…", color = Color.White)
        }
    }
}

@Composable
private fun SuccessOverlay(
    result: OcrResult,
    onViewResult: (OcrResult) -> Unit,
    onRetake: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "약 ${result.recognizedMedicineNames.size}개를 인식했습니다.",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { onViewResult(result) }) {
                Text("결과 보기")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRetake) {
                Text("다시 촬영")
            }
        }
    }
}

@Composable
private fun BoxScope.ErrorBanner(message: String) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = "오류: $message",
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun BoxScope.CaptureButton(
    hasCameraPermission: Boolean,
    imageCaptureRef: State<ImageCapture?>,
    context: Context,
    onImageCaptured: (Uri) -> Unit,
    onCaptureFailed: (String) -> Unit,
) {
    Button(
        onClick = {
            val imageCapture = imageCaptureRef.value ?: return@Button
            val photoFile =
                File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "prescription_${System.currentTimeMillis()}.jpg",
                )
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val uri =
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile,
                            )
                        onImageCaptured(uri)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        onCaptureFailed(exception.message ?: "촬영 실패")
                    }
                },
            )
        },
        modifier =
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
        enabled = hasCameraPermission && imageCaptureRef.value != null,
    ) {
        Text("촬영")
    }
}
