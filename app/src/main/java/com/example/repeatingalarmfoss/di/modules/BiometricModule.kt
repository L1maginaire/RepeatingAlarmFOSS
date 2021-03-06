package com.example.repeatingalarmfoss.di.modules

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.di.scopes.BiometricScope
import com.example.repeatingalarmfoss.helper.FlightRecorder
import dagger.Module
import dagger.Provides
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Named

private const val KEY_NAME = "keyName"

/*TODO investigate DaggerLazy*/
/*TODO support PINs*/
@Module
class BiometricModule(private val activity: FragmentActivity, private val onSuccessfulAuth: () -> Unit, private val onFailedAuth: () -> Unit = {}) {
    @BiometricScope
    @Provides
    fun provideCipher(@Named(KEY_NAME) keyName: String, keyStore: KeyStore?): Cipher? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
            .apply {
                keyStore?.load(null)
                init(Cipher.ENCRYPT_MODE, keyStore?.getKey(keyName, null) as SecretKey)
            }
    } else null

    @BiometricScope
    @Provides
    fun provideKeyStore(@Named(KEY_NAME) keyName: String): KeyStore? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        KeyStore.getInstance("AndroidKeyStore")
            .apply {
                load(null)
                with(KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")) {
                    init(
                        KeyGenParameterSpec.Builder(keyName, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            .setUserAuthenticationRequired(true)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                            .build()
                    )
                    generateKey()
                }
            }
    } else null

    @BiometricScope
    @Provides
    fun provideAuthenticator(context: Context, promptInfo: BiometricPrompt.PromptInfo?, authCallback: BiometricPrompt.AuthenticationCallback?, cipher: Cipher?): Authenticator = object : Authenticator {
        override fun authenticate() {
            if (BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), authCallback!!).authenticate(promptInfo!!, BiometricPrompt.CryptoObject(cipher!!))
            }
        }
    }

    @BiometricScope
    @Provides
    @Named(KEY_NAME)
    fun provideKeyName(): String = UUID.randomUUID().toString()

    @BiometricScope
    @Provides
    fun provideBiometricDialog(context: Context): BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(context.getString(R.string.title_records_access))
        .setDescription(context.getString(R.string.title_fingerprint_instruction))
        .setNegativeButtonText(context.getString(android.R.string.cancel))
        .build()

    @BiometricScope
    @Provides
    fun provideAuthCallback(biometricCallback: BiometricCallback): BiometricPrompt.AuthenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = biometricCallback.onAuthenticationSuccessful()
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = biometricCallback.onAuthenticationError(errorCode, errString)
        override fun onAuthenticationFailed() = biometricCallback.onAuthenticationFailed()
    }

    @BiometricScope
    @Provides
    fun provideLoggingCallback(logger: FlightRecorder): BiometricCallback = object : BiometricCallback {
        override fun onAuthenticationFailed() = onFailedAuth.invoke().also { logger.i { "biometric auth failed" } }
        override fun onAuthenticationSuccessful() = onSuccessfulAuth.invoke().also { logger.i { "biometric auth succeed" } }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onFailedAuth.invoke().also { logger.i { "biometric auth error. errorCode: $errorCode, errString: $errString" } }
    }
}

@BiometricScope
interface Authenticator {
    fun authenticate()
}

interface BiometricCallback {
    fun onAuthenticationFailed()
    fun onAuthenticationSuccessful()
    fun onAuthenticationError(errorCode: Int, errString: CharSequence)
}
