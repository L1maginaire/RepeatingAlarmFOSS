package com.example.repeatingalarmfoss.screens.biometric

interface BiometricCallback {
    fun onSdkVersionNotSupported()
    fun onBiometricSensorMissing()
    fun onBiometricAuthenticationNotAvailable()
    fun onBiometricAuthenticationPermissionNotGranted()
    fun onAuthenticationFailed()
    fun onAuthenticationCancelled()
    fun onAuthenticationSuccessful()
    fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence)
    fun onAuthenticationError(errorCode: Int, errString: CharSequence)
}
