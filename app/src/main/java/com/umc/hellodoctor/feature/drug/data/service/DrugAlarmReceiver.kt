package com.umc.hellodoctor.feature.drug.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.Room
import com.umc.hellodoctor.core.notification.NotificationHelper
import com.umc.hellodoctor.feature.drug.data.database.DrugDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DrugAlarmReceiver : BroadcastReceiver() {
    @Suppress("TooGenericExceptionCaught")
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val planId = intent.getStringExtra("planId") ?: return
        val alarmId = intent.getIntExtra("alarmId", -1)

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val db =
                    Room.databaseBuilder(
                        context,
                        DrugDatabase::class.java,
                        "drug_database",
                    ).build()
                val plan = db.drugPlanDao().getPlanById(planId)

                val message =
                    if (plan != null && plan.medicines.isNotEmpty()) {
                        val medicineNames = plan.medicines.joinToString(", ") { it.medicineName }
                        "[$medicineNames] 복용 시간입니다"
                    } else {
                        "약을 복용할 시간입니다"
                    }

                NotificationHelper.showNotification(
                    context = context,
                    notificationId = alarmId,
                    title = "복약 알림",
                    message = message,
                )
            } catch (e: Exception) {
                Log.e("DrugAlarmReceiver", "Error showing notification", e)
                // 예외 발생 시 기본 알림 표시
                NotificationHelper.showNotification(
                    context = context,
                    notificationId = alarmId,
                    title = "복약 알림",
                    message = "약을 복용할 시간입니다",
                )
            }
        }
    }
}
