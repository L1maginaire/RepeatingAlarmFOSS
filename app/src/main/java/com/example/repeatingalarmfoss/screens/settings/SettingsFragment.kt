package com.example.repeatingalarmfoss.screens.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.di.modules.Authenticator
import com.example.repeatingalarmfoss.di.modules.BiometricModule
import com.example.repeatingalarmfoss.di.scopes.BiometricScope
import com.example.repeatingalarmfoss.helper.extensions.getBooleanOf
import com.example.repeatingalarmfoss.helper.extensions.getStringOf
import java.util.*
import javax.inject.Inject

@BiometricScope
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    @Inject lateinit var authenticator: Authenticator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = super.onCreateView(inflater, container, savedInstanceState)
        .also { PreferenceManager.getDefaultSharedPreferences(requireContext()).registerOnSharedPreferenceChangeListener(this) }
        .also {
            val biometricPref = findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!
            when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> {
                    biometricPref.isEnabled = false
                    biometricPref.summary = getString(R.string.summary_version_is_not_supported)
                }
                BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ||
                        BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                            biometricPref.isEnabled = false
                            biometricPref.summary = getString(R.string.summary_lack_of_fingerprint_sensor)
                }
                BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    biometricPref.isEnabled = false
                    biometricPref.summary = getString(R.string.summary_sensor_unavailable)
                }
                BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                    biometricPref.isEnabled = false
                    biometricPref.summary = getString(R.string.summary_security_vulnerability)
                }
                BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                    biometricPref.isEnabled = false
                    biometricPref.summary = getString(R.string.unknown_error)
                }
                BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    biometricPref.isEnabled = false
                    biometricPref.summary = getString(R.string.summary_you_dont_have_fingerprint_presented)
                }
            }
        }

    override fun onAttach(context: Context) = super.onAttach(context).also {
        (requireContext().applicationContext as RepeatingAlarmApp)
            .appComponent
            .biometricComponent(
                BiometricModule(requireActivity(),
                    onSuccessfulAuth =  { findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!.isChecked = false },
                    onFailedAuth = { findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!.isChecked = true }
                )
            )
            .inject(this)
    }

    override fun onDestroyView() = super.onDestroyView().also { PreferenceManager.getDefaultSharedPreferences(requireContext()).unregisterOnSharedPreferenceChangeListener(this) }
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) = setPreferencesFromResource(R.xml.preferences, rootKey)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            getString(R.string.pref_lang) -> {
                val newLocale = Locale(sharedPreferences.getStringOf(key)!!)
                Locale.setDefault(newLocale)
                @Suppress("DEPRECATION") requireActivity().resources.updateConfiguration(resources.configuration.apply { setLocale(newLocale) }, resources.displayMetrics)
                requireActivity().recreate()
            }
            getString(R.string.pref_theme) -> {
                AppCompatDelegate.setDefaultNightMode(sharedPreferences.getStringOf(key)!!.toInt())
                requireActivity().recreate()
            }
            getString(R.string.pref_enable_biometric_protection) -> {
                if (sharedPreferences.getBooleanOf(key).not()) {
                    authenticator.authenticate()
                }
            }
        }
    }
}