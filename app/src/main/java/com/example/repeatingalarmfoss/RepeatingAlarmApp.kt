package com.example.repeatingalarmfoss

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.example.repeatingalarmfoss.di.components.AppComponent
import com.example.repeatingalarmfoss.di.components.DaggerAppComponent
import com.example.repeatingalarmfoss.helper.extensions.*
import com.example.repeatingalarmfoss.receivers.LowBatteryTracker
import es.dmoral.toasty.Toasty
import java.util.*

class RepeatingAlarmApp : MultiDexApplication(), LifecycleObserver {
    lateinit var appComponent: AppComponent

    var isAppInForeground = false

    override fun onCreate() {
        getDefaultSharedPreferences().incrementAppLaunchCounter()
        setupDagger()
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        createMissedAlarmNotificationChannel()

        if (getDefaultSharedPreferences().getBooleanOf(PREF_LOW_BATTERY_DND_AT_NIGHT).not()) {
            cancelLowBatteryChecker()
            scheduleLowBatteryChecker()
        }

        Toasty.Config.getInstance().apply()
    }

    private fun setupDagger() {
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }

    @SuppressLint("WrongConstant")
    private fun createMissedAlarmNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NotificationsManager.CHANNEL_MISSED_ALARM, getString(R.string.app_name), NotificationManager.IMPORTANCE_MAX)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        isAppInForeground = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        isAppInForeground = true
    }

    override fun attachBaseContext(base: Context) = super.attachBaseContext(base.provideUpdatedContextWithNewLocale(defaultLocale = Locale.getDefault().language))

    override fun onConfigurationChanged(newConfig: Configuration) {
        val newLocale = Locale(getDefaultSharedPreferences().getStringOf(PREF_APP_LANG) ?: Locale.UK.language)
        Locale.setDefault(newLocale)
        newConfig.setLocale(newLocale)
        super.onConfigurationChanged(newConfig)
    }
}