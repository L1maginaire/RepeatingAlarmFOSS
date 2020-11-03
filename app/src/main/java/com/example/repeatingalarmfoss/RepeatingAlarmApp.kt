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

class RepeatingAlarmApp: Application(), LifecycleObserver {
    var isAppInForeground = false
    lateinit var taskRepository: TaskRepository

    companion object { lateinit var INSTANCE: RepeatingAlarmApp }

    init { INSTANCE = this }

    private val migration1to2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) = database.execSQL("ALTER TABLE Task ADD COLUMN repeatingClassifier TEXT DEFAULT \"DAY_OF_WEEK\" NOT NULL")
            .also { database.execSQL("ALTER TABLE Task ADD COLUMN repeatingClassifierValue TEXT NOT NULL") }
    }

    private val migration2to3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) = database.execSQL("ALTER TABLE Task ADD COLUMN time TEXT NOT NULL")
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        taskRepository = Room.databaseBuilder(applicationContext, TasksDb::class.java, "database-name")
            .addMigrations(migration1to2)
            .addMigrations(migration2to3)
            .build().taskRepository()

        createMissedAlarmNotificationChannel()
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