package com.example.repeatingalarmfoss.di.components

import com.example.repeatingalarmfoss.AlarmReceiver
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.di.ViewModelBuilderModule
import com.example.repeatingalarmfoss.di.modules.ContextModule
import com.example.repeatingalarmfoss.di.modules.DatabaseModule
import com.example.repeatingalarmfoss.di.modules.LoggerModule
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.screens.added_tasks.DatePickerFragment
import com.example.repeatingalarmfoss.screens.added_tasks.SetupAddingTaskDialog
import com.example.repeatingalarmfoss.screens.added_tasks.TaskListActivity
import dagger.BindsInstance
import dagger.Component
import java.io.File
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
    fun inject(fragment: SetupAddingTaskDialog)
}