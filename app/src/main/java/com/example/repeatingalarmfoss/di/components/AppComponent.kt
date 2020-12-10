package com.example.repeatingalarmfoss.di.components

import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.base.BaseActivityViewModel
import com.example.repeatingalarmfoss.di.modules.*
import com.example.repeatingalarmfoss.receivers.AlarmReceiver
import com.example.repeatingalarmfoss.receivers.BootReceiver
import com.example.repeatingalarmfoss.receivers.LowBatteryTracker
import com.example.repeatingalarmfoss.screens.added_tasks.SetupAddingTaskFragment
import com.example.repeatingalarmfoss.screens.logs.LogActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DatabaseModule::class, ContextModule::class, LoggerModule::class, SchedulerModule::class, ServiceModule::class])
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
    fun inject(receiver: LowBatteryTracker)
    fun inject(fragment: SetupAddingTaskFragment)
    fun inject(activity: LogActivity)
    fun inject(activity: BaseActivityViewModel)
}
