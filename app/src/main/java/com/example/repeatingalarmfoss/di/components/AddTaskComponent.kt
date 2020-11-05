package com.example.repeatingalarmfoss.di.components

import com.example.repeatingalarmfoss.di.modules.AddTaskModule
import com.example.repeatingalarmfoss.screens.added_tasks.TaskListActivity
import dagger.Subcomponent

@Subcomponent(modules = [AddTaskModule::class])
interface AddTaskComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): AddTaskComponent
    }
    fun inject(activity: TaskListActivity)
}