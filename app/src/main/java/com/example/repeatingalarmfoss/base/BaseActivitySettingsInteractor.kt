package com.example.repeatingalarmfoss.base

import android.content.SharedPreferences
import com.example.repeatingalarmfoss.helper.extensions.PREF_APP_LANG
import com.example.repeatingalarmfoss.helper.extensions.PREF_APP_THEME
import com.example.repeatingalarmfoss.helper.extensions.getStringOf
import com.example.repeatingalarmfoss.helper.extensions.writeStringOf
import com.example.repeatingalarmfoss.helper.rx.BaseComposers
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject

class BaseActivitySettingsInteractor @Inject constructor(private val sharedPreferences: SharedPreferences, private val baseComposers: BaseComposers) {
    fun handleThemeChanges(toBeCompared: Int): Maybe<NightModeChangesResult> = Single.fromCallable { kotlin.runCatching { sharedPreferences.getStringOf(PREF_APP_THEME) }.getOrNull() }
        .filter { it != toBeCompared.toString() }
        .map { it.toInt() }
        .map<NightModeChangesResult> { NightModeChangesResult.Success(it) }
        .onErrorReturn {
            if (it is NullPointerException) {
                try {
                    sharedPreferences.writeStringOf(PREF_APP_THEME, toBeCompared.toString())
                    NightModeChangesResult.Success(toBeCompared)
                } catch (e: Throwable) {
                    NightModeChangesResult.SharedChangesCorruptionError
                }
            } else {
                NightModeChangesResult.SharedChangesCorruptionError
            }
        }.onErrorReturn { NightModeChangesResult.SharedChangesCorruptionError }
        .doOnError { it.printStackTrace() }
        .compose(baseComposers.commonMaybeFetchTransformer())

    fun checkLocaleChanged(currentLocale: String): Maybe<LocaleChangedResult> = Single.just(currentLocale)
        .filter { sharedPreferences.getStringOf(PREF_APP_LANG).equals(it).not() }
        .map<LocaleChangedResult> { LocaleChangedResult.Success }
        .onErrorReturn { LocaleChangedResult.SharedPreferencesCorruptionError }
}

sealed class NightModeChangesResult {
    data class Success(val code: Int) : NightModeChangesResult()
    object SharedChangesCorruptionError : NightModeChangesResult()
}

sealed class LocaleChangedResult {
    object Success : LocaleChangedResult()
    object SharedPreferencesCorruptionError : LocaleChangedResult()
}
