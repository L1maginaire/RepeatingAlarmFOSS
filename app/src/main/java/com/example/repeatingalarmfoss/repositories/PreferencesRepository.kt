package com.example.repeatingalarmfoss.repositories

import android.content.SharedPreferences
import com.example.repeatingalarmfoss.helper.extensions.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toCompletable
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private const val LAUNCH_COUNTER_THRESHOLD = 5

class PreferencesRepository @Inject constructor(private val sharedPreferences: SharedPreferences) {
    fun isForbiddenToNotifyLowBatteryAtNight(time: String = now()): Single<PermissionToNotifyAboutLowBatteryResult> = Single.just(PREF_LOW_BATTERY_DND_AT_NIGHT)
        /** DO NOT DISTURB == NOT PERMITTED TO NOTIFY!*/
        .map { sharedPreferences.getBoolean(it, true).not() || (sharedPreferences.getBoolean(it, true) && isTimeBetweenTwoTime("00:00", "09:00", time).not()) }
        .map<PermissionToNotifyAboutLowBatteryResult> { PermissionToNotifyAboutLowBatteryResult.Success(it) }
        .onErrorReturn { PermissionToNotifyAboutLowBatteryResult.Failure }

    fun incrementAppLaunchCounter() = sharedPreferences.incrementAppLaunchCounter()

    fun getPersistedLocale(): Single<PersistedLocaleResult> = Single.just(PREF_APP_LANG)
        .map { Locale(sharedPreferences.getStringOf(PREF_APP_LANG) ?: Locale.UK.language) }
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

