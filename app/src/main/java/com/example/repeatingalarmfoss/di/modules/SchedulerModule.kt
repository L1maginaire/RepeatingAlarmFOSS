package com.example.repeatingalarmfoss.di.modules

import com.example.repeatingalarmfoss.helper.rx.BaseSchedulerProvider
import com.example.repeatingalarmfoss.helper.rx.SchedulersProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SchedulerModule {
    @Provides
    @Singleton
    fun provideSchedulers(): SchedulersProvider = BaseSchedulerProvider()
}
