package com.example.repeatingalarmfoss.di.modules

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.os.BatteryManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ServiceModule {
    @SuppressLint("InlinedApi")
    @Provides
    @Singleton
    fun provideBatteryManager(context: Context): BatteryManager? = context.getSystemService(BATTERY_SERVICE) as BatteryManager?
}