package com.example.repeatingalarmfoss.screens.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.base.BaseActivity
import com.example.repeatingalarmfoss.helper.extensions.PREF_APP_LANG
import com.example.repeatingalarmfoss.helper.extensions.PREF_APP_THEME
import com.example.repeatingalarmfoss.helper.extensions.getStringOf
import java.util.*

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> true.also { finish() }
        else -> super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = super.onCreateView(inflater, container, savedInstanceState)
            .also { PreferenceManager.getDefaultSharedPreferences(requireContext()).registerOnSharedPreferenceChangeListener(this) }

        override fun onDestroyView() = super.onDestroyView().also { PreferenceManager.getDefaultSharedPreferences(requireContext()).unregisterOnSharedPreferenceChangeListener(this) }
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) = setPreferencesFromResource(R.xml.preferences, rootKey)

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            when (key) {
                PREF_APP_LANG -> {
                    val newLocale = Locale(sharedPreferences.getStringOf(key)!!)
                    Locale.setDefault(newLocale)
                    @Suppress("DEPRECATION") requireActivity().resources.updateConfiguration(resources.configuration.apply { setLocale(newLocale) }, resources.displayMetrics)
                    requireActivity().recreate()
                }
                PREF_APP_THEME -> {
                    AppCompatDelegate.setDefaultNightMode(sharedPreferences.getStringOf(PREF_APP_THEME)!!.toInt())
                    requireActivity().recreate()
                }
            }
        }
    }
}