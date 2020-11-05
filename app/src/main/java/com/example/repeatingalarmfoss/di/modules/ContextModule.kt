package com.example.repeatingalarmfoss.di.modules

import android.content.Context
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContextModule {
    @Provides
    @Singleton
    fun bindContext(app: RepeatingAlarmApp): Context = app.applicationContext
}
