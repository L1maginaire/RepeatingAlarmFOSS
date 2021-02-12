package com.example.repeatingalarmfoss.helper.extensions

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.base.ForegroundService.Companion.ACTION_SHOW_NOTIFICATION
import com.example.repeatingalarmfoss.base.ForegroundService.Companion.ACTION_STOP_FOREGROUND
import java.util.*

fun Context.provideUpdatedContextWithNewLocale(
    persistedLanguage: String? = kotlin.runCatching { getDefaultSharedPreferences().getStringOf(applicationContext.getString(R.string.pref_lang)) }.getOrNull(),
    defaultLocale: String? = null
): Context { /*TODO: RTL*/
    val locales = resources.getStringArray(R.array.supported_locales)
    val newLocale = Locale(locales.firstOrNull { it == persistedLanguage } ?: locales.firstOrNull { it == defaultLocale } ?: Locale.UK.language)
    getDefaultSharedPreferences().writeStringOf(getString(R.string.pref_lang), newLocale.language)
    Locale.setDefault(newLocale)
    return createConfigurationContext(Configuration().apply { setLocale(newLocale) })
}

fun Context.getDefaultSharedPreferences(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

@Suppress("DEPRECATION")
fun Configuration.getLocalesLanguage(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) locales[0].language else locale.language

/** workaround for Android 10 restrictions to launch activities in background:
 *  https://developer.android.com/guide/components/activities/background-starts
 * */
fun Context.activityImplicitLaunch(service: Class<out Service>, activity: Class<out Activity>, bundle: Bundle? = null) {
    if (Build.VERSION.SDK_INT >= 29 && (applicationContext as RepeatingAlarmApp).isAppInForeground.not()) {
        ContextCompat.startForegroundService(this, Intent(this, service).apply {
            action = ACTION_SHOW_NOTIFICATION
            bundle?.let { putExtras(it) }
        })
    } else {
        startService(Intent(this, service).apply {
            bundle?.let { putExtras(it) }
            action = ACTION_STOP_FOREGROUND
        })
        startActivity(Intent(this, activity).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            bundle?.let { putExtras(it) }
        })
    }
}