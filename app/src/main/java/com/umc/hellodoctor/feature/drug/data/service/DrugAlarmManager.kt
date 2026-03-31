package com.umc.hellodoctor.feature.drug.data.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.umc.hellodoctor.feature.drug.data.database.AlarmInfo
import java.util.Calendar

class DrugAlarmManager(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarms(
        planId: String,
        alarms: List<AlarmInfo>,
    ) {
        alarms.forEach { alarm ->
            scheduleAlarm(planId, alarm)
        }
    }

    private fun scheduleAlarm(
        planId: String,
        alarm: AlarmInfo,
    ) {
        val calendar =
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)

                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

        val intent =
            Intent(context, DrugAlarmReceiver::class.java).apply {
                putExtra("planId", planId)
                putExtra("alarmId", alarm.alarmId)
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                alarm.alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent,
                    )
                } else {
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent,
                    )
                }
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent,
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAlarms(
        planId: String,
        alarms: List<AlarmInfo>,
    ) {
        alarms.forEach { alarm ->
            cancelAlarm(alarm.alarmId)
        }
    }

    private fun cancelAlarm(alarmId: Int) {
        val intent = Intent(context, DrugAlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        alarmManager.cancel(pendingIntent)
    }
}
