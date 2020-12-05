package com.example.repeatingalarmfoss.di.modules

import android.content.Context
import android.content.SharedPreferences
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.helper.extensions.getDefaultSharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContextModule {
    @Provides
    @Singleton
    fun bindContext(app: RepeatingAlarmApp): Context = app.applicationContext

    @Provides
    @Singleton
    fun bindSharedPrefs(context: Context): SharedPreferences = context.getDefaultSharedPreferences()
}
