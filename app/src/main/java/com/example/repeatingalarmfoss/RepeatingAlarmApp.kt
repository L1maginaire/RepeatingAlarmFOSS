package com.example.repeatingalarmfoss

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.*
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.example.repeatingalarmfoss.di.components.AppComponent
import com.example.repeatingalarmfoss.di.components.DaggerAppComponent
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.*
import com.example.repeatingalarmfoss.repositories.PersistedLocaleResult
import com.example.repeatingalarmfoss.repositories.PreferencesRepository
import es.dmoral.toasty.Toasty
import io.reactivex.plugins.RxJavaPlugins
import java.util.*
import javax.inject.Inject

class RepeatingAlarmApp : MultiDexApplication(), LifecycleObserver {
    @Inject @JvmField var notificationManager: NotificationManager? = null
    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var logger: FlightRecorder
    lateinit var appComponent: AppComponent
    var isAppInForeground = false

    override fun onCreate() {
        setupDagger()
        super.onCreate()
        registerActivityStateLogger()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        preferencesRepository.incrementAppLaunchCounter()
        createNotificationChannels()
        cancelLowBatteryChecker().also { scheduleLowBatteryChecker() }
        Toasty.Config.getInstance().apply()
        RxJavaPlugins.setErrorHandler { logger.e(label = "GLOBAL", stackTrace = it.stackTrace) }
    }

    private fun registerActivityStateLogger() {
        registerActivityLifecycleCallbacks(object: ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) = logger.i { "${activity.javaClass.simpleName} paused" }
            override fun onActivityStarted(activity: Activity) = logger.i { "${activity.javaClass.simpleName} started" }
            override fun onActivityDestroyed(activity: Activity) = logger.i { "${activity.javaClass.simpleName} destroyed" }
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = logger.i { "${activity.javaClass.simpleName} saving instance state" }
            override fun onActivityStopped(activity: Activity) = logger.i { "${activity.javaClass.simpleName} stopped" }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = logger.i { "${activity.javaClass.simpleName} created" }
            override fun onActivityResumed(activity: Activity) = logger.i { "${activity.javaClass.simpleName} resumed" }
        })
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
                    NotificationChannel(CHANNEL_ALARM, getString(R.string.title_channel_notifications), NotificationManager.IMPORTANCE_MAX),
                    NotificationChannel(CHANNEL_MISSED_ALARM, getString(R.string.title_channel_missed_notifications), NotificationManager.IMPORTANCE_MAX),
                    NotificationChannel(CHANNEL_BATTERY_LOW_ID, getString(R.string.title_channel_low_battery), NotificationManager.IMPORTANCE_MAX)
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