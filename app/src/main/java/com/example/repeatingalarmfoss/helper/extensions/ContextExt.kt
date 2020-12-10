package com.example.repeatingalarmfoss.helper.extensions

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.screens.alarm.ALARM_ARG_TITLE
import com.example.repeatingalarmfoss.screens.low_battery.LowBatteryNotifierActivity
import com.example.repeatingalarmfoss.services.LowBatteryNotificationService
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

/** workaround for Android 10 restrictions to launch activities in background:
 * https://developer.android.com/guide/components/activities/background-starts
 * */
fun Context.activityImplicitLaunch(service: Class<out Service>, activity: Class<out Activity>, extraName: String? = null, extraValue: String? = null /*todo: afterwards should be implemented Bundle*/) {
    if (Build.VERSION.SDK_INT >= 29 && (applicationContext as RepeatingAlarmApp).isAppInForeground.not()) {
        ContextCompat.startForegroundService(this, Intent(this, service))
    } else {
        startActivity(Intent(this, activity).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if(extraName.isNullOrBlank().not() && extraValue.isNullOrBlank().not()) {
                putExtra(extraName, extraValue)
            }
        })
    }
}