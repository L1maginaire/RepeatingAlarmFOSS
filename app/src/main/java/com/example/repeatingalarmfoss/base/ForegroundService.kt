package com.example.repeatingalarmfoss.base

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.helper.Notifier
import com.example.repeatingalarmfoss.helper.extensions.toColorfulString
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class ForegroundService : BaseService() {
    companion object {
        const val ACTION_TERMINATE = "ForegroundService.ACTION_TERMINATE"
        const val ACTION_STOP_FOREGROUND = "ForegroundService.STOP_FOREGROUND"
        const val ACTION_SHOW_NOTIFICATION = "ForegroundService.SHOW_NOTIFICATION"
    }

    @Inject lateinit var notifier: Notifier
    protected val timer: Observable<Long> = Observable.timer(10, TimeUnit.MINUTES, AndroidSchedulers.mainThread())

    abstract fun getActivity(): Class<out Activity>
    abstract fun getTitle(intent: Intent?): String
    abstract fun getServiceId(): Int
    @DrawableRes abstract fun getIcon(): Int

    override fun onCreate() {
        super.onCreate()
        (application as RepeatingAlarmApp).appComponent.inject(this)
        notifier.start()
    }

    protected fun getCancelAction(): NotificationCompat.Action = NotificationCompat.Action(
        0,
        getString(R.string.dismiss).toColorfulString(Color.RED),
        PendingIntent.getService(this, 0, Intent(this, this::class.java).apply { this.action = ACTION_TERMINATE }, PendingIntent.FLAG_UPDATE_CURRENT)
    )

    protected fun getFullscreenIntent(bundle: Bundle, activity: Class<out Activity>): PendingIntent = PendingIntent.getActivity(
        this,
        0,
        Intent(this, activity)
            .apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtras(bundle)
            },
        0
    )

    override fun onDestroy() {
        super.onDestroy()
        notifier.stop()
    }
}