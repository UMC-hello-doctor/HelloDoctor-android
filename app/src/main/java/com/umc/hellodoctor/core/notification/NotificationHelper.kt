package com.umc.hellodoctor.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.umc.hellodoctor.R
import com.umc.hellodoctor.app.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "hello_doctor_default"
    private const val CHANNEL_NAME = "일반 알림"

    // 앱에서 공통으로 쓸 기본 알람 ID/문구
    private const val DEFAULT_NOTIFICATION_ID = 1001
    private const val DEFAULT_TITLE = "HelloDoctor 알림"
    private const val DEFAULT_MESSAGE = "설정한 시간이 되었습니다."

    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val largeIcon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.welcome_logo   // 컬러 앱 로고
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.welcome_logo)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    // 기본 알람 (문구 커스텀 가능)
    fun showDefaultAlarm(
        context: Context,
        message: String = DEFAULT_MESSAGE
    ) {
        showNotification(
            context = context,
            notificationId = DEFAULT_NOTIFICATION_ID,
            title = DEFAULT_TITLE,
            message = message
        )
    }
}
