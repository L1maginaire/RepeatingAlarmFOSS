package com.example.repeatingalarmfoss.base

import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.example.repeatingalarmfoss.R

abstract class ForegroundService : Service() {
    companion object {
        private const val TERMINATE = "ACTION_TERMINATE"
    }

    override fun onBind(intent: Intent?): IBinder? = null
    abstract fun getBundle(intent: Intent?): Bundle?
    abstract fun getActivity(): Class<out Activity>
    abstract fun getServiceId(): Int
    abstract fun getTitle(intent: Intent?): String
    abstract fun getChannelId(): String
    @DrawableRes abstract fun getIcon(): Int

    @CallSuper
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY.also {
        if (intent?.action == TERMINATE) {
            stopSelf()
        } else {
            val notificationBuilder = NotificationCompat.Builder(this, getChannelId())
                .setSmallIcon(getIcon())
                .setOngoing(true)
                .setContentText(getTitle(intent))
                .addAction(getCancelAction())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(getFullscreenIntent(getBundle(intent), getActivity()), true)
                .setLargeIcon(BitmapFactory.decodeResource(resources, getIcon()))
            startForeground(getServiceId(), notificationBuilder.build())
        }
    }

    private fun getCancelAction(): NotificationCompat.Action = NotificationCompat.Action(
        0,
        SpannableString(getString(R.string.dismiss)).apply { setSpan(ForegroundColorSpan(Color.RED), 0, getString(R.string.dismiss).length, 0) },
        PendingIntent.getService(this, 0, Intent(this, this::class.java).apply { this.action = TERMINATE }, PendingIntent.FLAG_UPDATE_CURRENT)
    )

    private fun getFullscreenIntent(bundle: Bundle?, activity: Class<out Activity>): PendingIntent = PendingIntent.getActivity(
        this,
        0,
        Intent(this, activity)
            .apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                bundle?.also { putExtras(it) }
            },
        0
    )

    override fun onDestroy() {
        super.onDestroy()
        Log.d("a", "aaa ${this::javaClass} is destroyed")
    }
}