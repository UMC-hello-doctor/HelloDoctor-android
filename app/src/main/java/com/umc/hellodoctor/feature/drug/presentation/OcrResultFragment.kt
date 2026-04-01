package com.umc.hellodoctor.feature.drug.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                androidx.compose.ui.viewinterop.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                ocrResultScreen(
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

@Composable
internal fun ocrResultScreen(
    initialMedicineNames: List<String>,
    onConfirm: (List<String>) -> Unit,
    onBack: () -> Unit,
) {
    val medicineNames =
        remember {
            mutableStateListOf(*initialMedicineNames.toTypedArray())
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "인식된 약 목록",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (medicineNames.isEmpty()) {
            Text(
                text = "약 이름을 찾지 못했습니다.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) {
                itemsIndexed(medicineNames) { index, name ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = { medicineNames.removeAt(index) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "삭제",
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier =
                Modifier
                    .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f),
            ) {
                Text("돌아가기")
            }

            Button(
                onClick = { onConfirm(medicineNames) },
                enabled = medicineNames.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) {
                Text("처방전으로 추가")
            }
        }
    }
}
