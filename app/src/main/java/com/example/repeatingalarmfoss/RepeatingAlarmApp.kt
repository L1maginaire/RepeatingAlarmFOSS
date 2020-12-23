package com.example.repeatingalarmfoss

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import com.example.repeatingalarmfoss.repositories.PersistedLocaleResult
import com.example.repeatingalarmfoss.repositories.PreferencesRepository
import es.dmoral.toasty.Toasty
import java.util.*
import javax.inject.Inject

class RepeatingAlarmApp : MultiDexApplication(), LifecycleObserver {
    @Inject
    @JvmField
    var notificationManager: NotificationManager? = null

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    lateinit var appComponent: AppComponent

    var isAppInForeground = false

    override fun onCreate() {
        setupDagger()
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        preferencesRepository.incrementAppLaunchCounter()
        createNotificationChannels()
        cancelLowBatteryChecker().also { scheduleLowBatteryChecker() }
        Toasty.Config.getInstance().apply()
    }

    private fun setupDagger() {
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
            .apply { inject(this@RepeatingAlarmApp) }
    }

    @SuppressLint("WrongConstant")
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager!!.createNotificationChannels(
                listOf(
                    NotificationChannel(NotificationsManager.CHANNEL_ALARM, getString(R.string.title_channel_notifications), NotificationManager.IMPORTANCE_MAX),
                    NotificationChannel(NotificationsManager.CHANNEL_BATTERY_LOW_ID, getString(R.string.title_channel_low_battery), NotificationManager.IMPORTANCE_MAX)
                )
            )
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
        preferencesRepository.getPersistedLocale()
            .blockingGet().also {
                if (it is PersistedLocaleResult.Success) {
                    Locale.setDefault(it.locale)
                    newConfig.setLocale(it.locale)
                }
            }
        super.onConfigurationChanged(newConfig)
    }
}