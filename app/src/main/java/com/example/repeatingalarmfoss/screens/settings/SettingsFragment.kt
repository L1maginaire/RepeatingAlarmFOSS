package com.example.repeatingalarmfoss.screens.settings

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.helper.extensions.getStringOf
import java.util.*

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = super.onCreateView(inflater, container, savedInstanceState)
        .also { PreferenceManager.getDefaultSharedPreferences(requireContext()).registerOnSharedPreferenceChangeListener(this) }
        .also {
            val biometricPref = findPreference<SwitchPreferenceCompat>(getString(R.string.pref_enable_biometric_protection))!!
            when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> {
                    biometricPref.isEnabled = false
                    biometricPref.summary = getString(R.string.summary_version_is_not_supported)
                }
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED -> {
                    biometricPref.isEnabled = false
                    biometricPref.summary = getString(R.string.summary_lack_of_fingerprint_permission)
                }
                FingerprintManagerCompat.from(requireContext()).isHardwareDetected.not() -> {
                    biometricPref.isEnabled = false
                    biometricPref.summary = getString(R.string.summary_lack_of_fingerprint_sensor)
                }
                FingerprintManagerCompat.from(requireContext()).hasEnrolledFingerprints().not() -> {
                    biometricPref.isEnabled = false
                    biometricPref.summary = getString(R.string.summary_you_dont_have_fingerprint_presented)
                }
            }
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
        }
    }
}