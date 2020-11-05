package com.example.repeatingalarmfoss

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.repeatingalarmfoss.db.TaskRepository
import com.example.repeatingalarmfoss.db.TasksDb
import com.example.repeatingalarmfoss.di.components.AppComponent
import com.example.repeatingalarmfoss.di.components.DaggerAppComponent

class RepeatingAlarmApp: Application(), LifecycleObserver {
    lateinit var appComponent: AppComponent

    var isAppInForeground = false

    companion object { lateinit var INSTANCE: RepeatingAlarmApp }

    init { INSTANCE = this }

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
}