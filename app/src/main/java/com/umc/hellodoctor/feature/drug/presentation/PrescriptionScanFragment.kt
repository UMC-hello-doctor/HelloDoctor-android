package com.umc.hellodoctor.feature.drug.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrescriptionScanFragment : Fragment() {
    private val viewModel: PrescriptionScanViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        androidx.compose.ui.platform.ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val uiState by viewModel.uiState.observeAsState(PrescriptionScanUiState.Idle)
                PrescriptionScanScreen(
                    uiState = uiState,
                    callbacks = PrescriptionScanCallbacks(
                        onImageCaptured = { uri -> viewModel.onImageCaptured(uri) },
                        onCaptureFailed = { msg -> viewModel.onCaptureFailed(msg) },
                        onViewResult = { result ->
                            val names = result.recognizedMedicineNames.toTypedArray()
                            findNavController().navigate(
                                PrescriptionScanFragmentDirections
                                    .actionPrescriptionScanFragmentToOcrResultFragment(names),
                            )
                        },
                        onRetake = { viewModel.resetToIdle() },
                        onBack = { findNavController().popBackStack() },
                    ),
                )
            }
        }
}

