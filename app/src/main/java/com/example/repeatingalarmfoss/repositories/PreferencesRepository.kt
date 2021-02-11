package com.example.repeatingalarmfoss.repositories

import android.content.Context
import android.content.SharedPreferences
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.helper.extensions.*
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

private const val LAUNCH_COUNTER_THRESHOLD = 5

class PreferencesRepository @Inject constructor(private val sharedPreferences: SharedPreferences, private val context: Context) {
    fun isForbiddenToNotifyLowBatteryAtNight(time: String = now()): Single<PermissionToNotifyAboutLowBatteryResult> = Single.just(context.getString(R.string.pref_low_battery_dnd_time))
        /** DO NOT DISTURB == NOT PERMITTED TO NOTIFY!*/
        .map { sharedPreferences.getBoolean(it, true).not() || (sharedPreferences.getBoolean(it, true) && isTimeBetweenTwoTime("00:00", "09:00", time).not()) }
        .map<PermissionToNotifyAboutLowBatteryResult> { PermissionToNotifyAboutLowBatteryResult.Success(it) }
        .onErrorReturn { PermissionToNotifyAboutLowBatteryResult.Failure }

    fun incrementAppLaunchCounter() = sharedPreferences.incrementAppLaunchCounter()

    fun getPersistedLocale(): Single<PersistedLocaleResult> = Single.just(context.getString(R.string.pref_lang))
        .map { Locale(sharedPreferences.getStringOf(it) ?: Locale.UK.language) }
        .map<PersistedLocaleResult> { PersistedLocaleResult.Success(it) }
        .onErrorReturn { PersistedLocaleResult.Failure }

    fun isPermittedToShowRateDialog(): Single<PermissionToShowRateDialogResult> = Single.just(PREF_NEVER_SHOW_RATE_APP)
        .map { sharedPreferences.getBooleanOf(it).not() && sharedPreferences.getAppLaunchCounter() % LAUNCH_COUNTER_THRESHOLD == 0 }
        .map<PermissionToShowRateDialogResult> { PermissionToShowRateDialogResult.Success(it) }
        .onErrorReturn { PermissionToShowRateDialogResult.Failure }
}

sealed class PermissionToShowRateDialogResult {
    data class Success(val toShow: Boolean) : PermissionToShowRateDialogResult()
    object Failure : PermissionToShowRateDialogResult()
}

sealed class PersistedLocaleResult {
    data class Success(val locale: Locale) : PersistedLocaleResult()
    object Failure : PersistedLocaleResult()
}

sealed class PermissionToNotifyAboutLowBatteryResult {
    data class Success(var permitted: Boolean) : PermissionToNotifyAboutLowBatteryResult()
    object Failure : PermissionToNotifyAboutLowBatteryResult()
}

