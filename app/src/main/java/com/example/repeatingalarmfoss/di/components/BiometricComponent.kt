package com.example.repeatingalarmfoss.di.components

import com.example.repeatingalarmfoss.di.modules.BiometricModule
import com.example.repeatingalarmfoss.di.scopes.BiometricScope
import com.example.repeatingalarmfoss.screens.added_tasks.MainActivity
import com.example.repeatingalarmfoss.screens.settings.SettingsFragment
import dagger.Subcomponent

@BiometricScope
@Subcomponent(modules = [BiometricModule::class])
interface BiometricComponent {
    fun inject(activity: MainActivity)
    fun inject(fragment: SettingsFragment)
}