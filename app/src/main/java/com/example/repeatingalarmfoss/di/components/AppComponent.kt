package com.example.repeatingalarmfoss.di.components

import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.di.ViewModelBuilderModule
import com.example.repeatingalarmfoss.di.modules.ContextModule
import com.example.repeatingalarmfoss.di.modules.DatabaseModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DatabaseModule::class, ContextModule::class, ViewModelBuilderModule::class])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: RepeatingAlarmApp): Builder
        fun build(): AppComponent
    }

    fun addEditTaskComponent(): AddTaskComponent.Factory
}