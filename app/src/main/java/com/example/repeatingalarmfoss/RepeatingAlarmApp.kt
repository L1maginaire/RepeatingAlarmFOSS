package com.example.repeatingalarmfoss

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.repeatingalarmfoss.di.components.AppComponent
import com.example.repeatingalarmfoss.di.components.DaggerAppComponent
import com.example.repeatingalarmfoss.helper.extensions.*
import java.util.*

class RepeatingAlarmApp : Application(), LifecycleObserver {
    lateinit var appComponent: AppComponent

    var isAppInForeground = false

    override fun onCreate() {
        setupDagger()
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        createMissedAlarmNotificationChannel()
    }

    private fun setupDagger() {
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }

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

    override fun attachBaseContext(base: Context) = super.attachBaseContext(base.provideUpdatedContextWithNewLocale())

    override fun onConfigurationChanged(newConfig: Configuration) {
        val newLocale = Locale(getDefaultSharedPreferences().getStringOf(PREF_APP_LANG) ?: Locale.UK.language)
        Locale.setDefault(newLocale)
        newConfig.setLocale(newLocale)
        super.onConfigurationChanged(newConfig)
    }
}