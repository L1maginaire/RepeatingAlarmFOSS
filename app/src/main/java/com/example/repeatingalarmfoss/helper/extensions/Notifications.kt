package com.example.repeatingalarmfoss.helper.extensions

import android.app.Notification
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

fun Context.constructNotification(
    channelID: String,
    title: String,
    @DrawableRes smallImage: Int,
    contentText: String? = null,
    priority: Int = NotificationCompat.PRIORITY_HIGH
): Notification = NotificationCompat.Builder(this, channelID)
    .setSmallIcon(smallImage)
    .setContentTitle(title)
    .setContentText(contentText)
    .setAutoCancel(true)
    .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
    .setPriority(priority)
    .build()

fun Context.showNotification(notification: Notification, notificationId: Long) = NotificationManagerCompat.from(this).notify(notificationId.toInt(), notification)