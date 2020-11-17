package com.example.repeatingalarmfoss.di.components

import com.example.repeatingalarmfoss.receivers.AlarmReceiver
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.di.ViewModelBuilderModule
import com.example.repeatingalarmfoss.di.modules.ContextModule
import com.example.repeatingalarmfoss.di.modules.DatabaseModule
import com.example.repeatingalarmfoss.di.modules.LoggerModule
import com.example.repeatingalarmfoss.receivers.BootReceiver
import com.example.repeatingalarmfoss.screens.added_tasks.SetupAddingTaskFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DatabaseModule::class, ContextModule::class, ViewModelBuilderModule::class, LoggerModule::class])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: RepeatingAlarmApp): Builder
        fun build(): AppComponent
    }

    fun addTaskComponent(): AddTaskComponent.Factory

    fun inject(receiver: AlarmReceiver)
    fun inject(receiver: BootReceiver)
    fun inject(fragment: SetupAddingTaskFragment)
}