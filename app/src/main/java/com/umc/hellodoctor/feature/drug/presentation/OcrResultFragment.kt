package com.umc.hellodoctor.feature.drug.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.umc.hellodoctor.feature.drug.data.model.MedicineSearchItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OcrResultFragment : Fragment() {
    private val args: OcrResultFragmentArgs by navArgs()

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
                OcrResultScreen(
                    initialMedicineNames = args.ocrMedicineNames.toList(),
                    onConfirm = { confirmedNames ->
                        val items =
                            confirmedNames.map { name ->
                                MedicineSearchItem(
                                    medicineId = "ocr_$name",
                                    medicineName = name,
                                    entpName = "",
                                    medicineImage = "",
                                    efficacy = "",
                                )
                            }.toTypedArray()
                        findNavController().navigate(
                            OcrResultFragmentDirections
                                .actionOcrResultFragmentToDrugInfoInputFragment(items, null),
                        )
                    },
                    onBack = { findNavController().popBackStack() },
                )
            }
        }
}
