package com.example.repeatingalarmfoss.screens.biometric

import com.example.repeatingalarmfoss.di.modules.BiometricScope

@BiometricScope
interface Authenticator {
    fun authenticate()
}
