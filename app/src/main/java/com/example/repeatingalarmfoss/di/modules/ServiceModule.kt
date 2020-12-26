package com.example.repeatingalarmfoss.di.modules

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.Context.*
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.BatteryManager
import android.os.PowerManager
import android.os.Vibrator
import android.view.WindowManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ServiceModule {
    @SuppressLint("InlinedApi")
    @Provides
    @Singleton
    fun provideBatteryManager(context: Context): BatteryManager? = context.getSystemService(BATTERY_SERVICE) as BatteryManager?

    @SuppressLint("InlinedApi")
    @Provides
    @Singleton
    fun provideCameraManager(context: Context): CameraManager? = context.getSystemService(CAMERA_SERVICE) as CameraManager?

    @Provides
    @Singleton
    fun provideVibrator(context: Context): Vibrator? = context.getSystemService(VIBRATOR_SERVICE) as Vibrator?

    @Provides
    @Singleton
    fun provideWakeLock(context: Context): PowerManager.WakeLock = (context.getSystemService(POWER_SERVICE) as PowerManager).newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, javaClass.simpleName)

    @Provides
    @Singleton
    fun provideSensorManager(context: Context): SensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager

    @Provides
    @Singleton
    fun provideAudioManager(context: Context): AudioManager? = context.getSystemService(AUDIO_SERVICE) as AudioManager?

    @Provides
    @Singleton
    fun provideKeyguardManager(context: Context): KeyguardManager = context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager

    @Provides
    @Singleton
    fun provideInfoAboutFlashFeature(context: Context): Boolean = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

    @Provides
    @Singleton
    fun provideAlarmManager(context: Context): AlarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

    @Provides
    @Singleton
    fun provideContentResolver(context: Context): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    fun provideNotificationService(context: Context): NotificationManager? = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
}