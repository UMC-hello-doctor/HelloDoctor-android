package com.umc.hellodoctor.feature.drug.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MedicineSearchItem(
    val medicineId: String,
    val medicineName: String,
    val entpName: String,
    val medicineImage: String,
    val efficacy: String
) : Parcelable
