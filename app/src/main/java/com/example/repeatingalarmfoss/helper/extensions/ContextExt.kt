package com.example.repeatingalarmfoss.helper.extensions

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.preference.PreferenceManager
import com.example.repeatingalarmfoss.R
import java.util.*

fun Context.provideUpdatedContextWithNewLocale(
    persistedLanguage: String? = kotlin.runCatching { getDefaultSharedPreferences().getStringOf(PREF_APP_LANG) }.getOrNull(),
    defaultLocale: String? = null
): Context { /*TODO RTL*/
    val locales = resources.getStringArray(R.array.supported_locales)
    val newLocale = Locale(locales.firstOrNull { it == persistedLanguage } ?: locales.firstOrNull { it == defaultLocale } ?: Locale.UK.language)
    getDefaultSharedPreferences().writeStringOf(PREF_APP_LANG, newLocale.language)
    Locale.setDefault(newLocale)
    return createConfigurationContext(Configuration().apply { setLocale(newLocale) })
}

fun Context.getDefaultSharedPreferences(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

@Suppress("DEPRECATION")
fun Configuration.getLocalesLanguage(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) locales[0].language else locale.language