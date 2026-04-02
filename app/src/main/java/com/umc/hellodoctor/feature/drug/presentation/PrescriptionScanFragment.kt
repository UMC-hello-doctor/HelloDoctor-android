package com.umc.hellodoctor.feature.drug.presentation

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.umc.hellodoctor.core.camera.CameraController
import com.umc.hellodoctor.feature.drug.domain.model.OcrResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrescriptionScanFragment : Fragment() {
    private val viewModel: PrescriptionScanViewModel by viewModels()
    private lateinit var cameraController: CameraController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraController =
            CameraController(
                fragment = this,
                onImageCaptured = { uri -> viewModel.onImageCaptured(uri) },
                onPermissionDenied = {
                    Toast.makeText(
                        requireContext(),
                        "카메라 권한이 필요합니다.",
                        Toast.LENGTH_LONG,
                    ).show()
                    findNavController().popBackStack()
                },
                onPermissionGranted = { /* bindCamera will be called in AndroidView factory */ },
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                val uiState by viewModel.uiState.observeAsState(PrescriptionScanUiState.Idle)
                PrescriptionScanScreen(
                    uiState = uiState,
                    onViewResult = { result ->
                        val names = result.recognizedMedicineNames.toTypedArray()
                        findNavController().navigate(
                            PrescriptionScanFragmentDirections
                                .actionPrescriptionScanFragmentToOcrResultFragment(names),
                        )
                    },
                    onBack = { findNavController().popBackStack() },
                    onResetToIdle = { viewModel.resetToIdle() },
                    cameraController = cameraController,
                    viewLifecycleOwner = viewLifecycleOwner,
                )
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraController.unbindCamera()
    }
}

@Suppress("FunctionName", "LongParameterList")
@Composable
internal fun PrescriptionScanScreen(
    uiState: PrescriptionScanUiState,
    onViewResult: (result: OcrResult) -> Unit,
    onBack: () -> Unit,
    onResetToIdle: () -> Unit,
    cameraController: CameraController,
    viewLifecycleOwner: LifecycleOwner,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        when (uiState) {
            PrescriptionScanUiState.Idle ->
                cameraPreviewScreen(
                    onTakePhoto = { cameraController.takePhoto() },
                    onBack = onBack,
                    cameraController = cameraController,
                    viewLifecycleOwner = viewLifecycleOwner,
                )

            PrescriptionScanUiState.Loading ->
                loadingScreen()

            is PrescriptionScanUiState.Success ->
                successScreen(
                    result = uiState.result,
                    onViewResult = onViewResult,
                    onRetry = onResetToIdle,
                )

            is PrescriptionScanUiState.Error ->
                errorScreen(
                    message = uiState.message,
                    onRetry = onResetToIdle,
                )
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun cameraPreviewScreen(
    onTakePhoto: () -> Unit,
    onBack: () -> Unit,
    cameraController: CameraController,
    viewLifecycleOwner: androidx.lifecycle.LifecycleOwner,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview layer
        AndroidView(
            factory = { context ->
                PreviewView(context).also { previewView ->
                    cameraController.bindCamera(viewLifecycleOwner, previewView)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Prescription guide overlay
        PrescriptionGuideOverlay(modifier = Modifier.fillMaxSize())

        // Back button (top-left)
        IconButton(
            onClick = onBack,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
            )
        }

        // Capture button (bottom-center)
        CaptureButton(
            onClick = onTakePhoto,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp),
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun PrescriptionGuideOverlay(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(
        modifier =
            modifier.graphicsLayer {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
            },
    ) {
        // Semi-transparent black background
        @Suppress("MagicNumber")
        val overlayAlpha = 0.5f
        drawRect(color = Color.Black.copy(alpha = overlayAlpha))

        // Calculate guide rectangle (90% of width, 1:1.4 aspect ratio - A4 ratio)
        @Suppress("MagicNumber")
        val guideWidthPercent = 0.9f

        @Suppress("MagicNumber")
        val guideHeightRatio = 1.4f
        val rectWidth = size.width * guideWidthPercent
        val rectHeight = rectWidth * guideHeightRatio
        val left = (size.width - rectWidth) / 2f
        val top = (size.height - rectHeight) / 2f

        // Cut out the guide area (only works with CompositingStrategy.Offscreen on API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(rectWidth, rectHeight),
                blendMode = BlendMode.Clear,
            )
        }

        // Guide rectangle border
        drawRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(rectWidth, rectHeight),
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

@Suppress("FunctionName")
@Composable
private fun CaptureButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(4.dp, Color.LightGray, CircleShape)
                .clickable(onClick = onClick),
    )
}

@Composable
private fun loadingScreen() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "처방전을 분석 중입니다…",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun successScreen(
    result: OcrResult,
    onViewResult: (OcrResult) -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "약 ${result.recognizedMedicineNames.size}개를 인식했습니다.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { onViewResult(result) }) {
            Text("결과 보기")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("다시 촬영")
        }
    }
}

@Composable
private fun errorScreen(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "❌",
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "인식 실패",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.height(48.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
        ) {
            Text("다시 촬영")
        }
    }
}
