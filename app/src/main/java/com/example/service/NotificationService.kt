package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NotificationService {

    fun showProximityNotification(context: Context, merchantId: String) {
        val channelId = "proximity_alerts"
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "تنبيهات القرب",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "إشعارات عند الاقتراب من المتجر"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("merchant_id", merchantId)
            putExtra("action", "open_coupon")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            // .setSmallIcon(R.drawable.ic_launcher_foreground) // Using a standard icon since we don't have R.drawable.ic_coupon
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🎉 أنت قريب من المقهى!")
            .setContentText("لا تنسَ قسائمك المجانية - افتح محفظتك الآن")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(merchantId.hashCode(), notification)
    }
}
