package com.umc.hellodoctor.feature.drug.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.umc.hellodoctor.core.camera.CameraController
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
                onPermissionDenied = { },
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        androidx.compose.ui.platform.ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                val uiState by viewModel.uiState.observeAsState(PrescriptionScanUiState.Idle)
                prescriptionScanScreen(
                    uiState = uiState,
                    onTakePhoto = { cameraController.checkPermissionAndOpenCamera() },
                    onViewResult = { result ->
                        val names = result.recognizedMedicineNames.toTypedArray()
                        findNavController().navigate(
                            PrescriptionScanFragmentDirections
                                .actionPrescriptionScanFragmentToOcrResultFragment(names),
                        )
                    },
                    onBack = { findNavController().popBackStack() },
                )
            }
        }
}

@Composable
internal fun prescriptionScanScreen(
    uiState: PrescriptionScanUiState,
    onTakePhoto: () -> Unit,
    onViewResult: (result: com.umc.hellodoctor.feature.drug.domain.model.OcrResult) -> Unit,
    onBack: () -> Unit,
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
            text = "처방전 스캔",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(32.dp))

        scanStateContent(uiState, onTakePhoto, onViewResult)

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack) {
            Text("돌아가기")
        }
    }
}

@Composable
private fun scanStateContent(
    uiState: PrescriptionScanUiState,
    onTakePhoto: () -> Unit,
    onViewResult: (result: com.umc.hellodoctor.feature.drug.domain.model.OcrResult) -> Unit,
) {
    when (uiState) {
        PrescriptionScanUiState.Idle -> {
            Text(
                text = "처방전을 촬영하여 약 이름을 자동으로 인식합니다.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onTakePhoto) {
                Text("사진 촬영")
            }
        }

        PrescriptionScanUiState.Loading -> {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("처방전을 분석 중입니다…")
        }

        is PrescriptionScanUiState.Success -> {
            Text(
                text = "약 ${uiState.result.recognizedMedicineNames.size}개를 인식했습니다.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { onViewResult(uiState.result) }) {
                Text("결과 보기")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onTakePhoto) {
                Text("다시 촬영")
            }
        }

        is PrescriptionScanUiState.Error -> {
            Text(
                text = "오류: ${uiState.message}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onTakePhoto) {
                Text("다시 시도")
            }
        }
    }
}
