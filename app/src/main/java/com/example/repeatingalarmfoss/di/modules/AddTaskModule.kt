package com.example.repeatingalarmfoss.di.modules

import androidx.lifecycle.ViewModel
import com.example.repeatingalarmfoss.di.ViewModelKey
import com.example.repeatingalarmfoss.screens.added_tasks.AddingTasksViewModel
import com.example.repeatingalarmfoss.screens.added_tasks.MainActivityViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AddTaskModule {
    @Binds
    @IntoMap
    @ViewModelKey(AddingTasksViewModel::class)
    abstract fun bindAddingTaskViewModel(viewModel: AddingTasksViewModel): ViewModel
}

@Module
abstract class MainActivityModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    abstract fun bindMainViewModel(viewModel: MainActivityViewModel): ViewModel
}