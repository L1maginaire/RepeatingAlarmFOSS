package com.example.repeatingalarmfoss.di.components

import com.example.repeatingalarmfoss.di.modules.BiometricModule
import com.example.repeatingalarmfoss.di.modules.BiometricScope
import com.example.repeatingalarmfoss.screens.added_tasks.MainActivity
import dagger.Subcomponent

@BiometricScope
@Subcomponent(modules = [BiometricModule::class])
interface BiometricComponent {
    fun inject(activity: MainActivity)
}