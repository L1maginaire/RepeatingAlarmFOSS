package com.example.repeatingalarmfoss.interactors

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate.*
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.getStringOf
import com.example.repeatingalarmfoss.helper.extensions.writeStringOf
import com.example.repeatingalarmfoss.helper.rx.BaseComposers
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject

class BaseActivitySettingsInteractor @Inject constructor(private val sharedPreferences: SharedPreferences, private val baseComposers: BaseComposers, private val logger: FlightRecorder, private val context: Context) {
    fun handleThemeChanges(toBeCompared: Int): Maybe<NightModeChangesResult> = Single.fromCallable { sharedPreferences.getStringOf(context.getString(R.string.pref_theme))!! }
        .map { it.toInt() }
        .filter { it != toBeCompared }
        .map<NightModeChangesResult> { if (it == MODE_NIGHT_FOLLOW_SYSTEM || it == MODE_NIGHT_NO  || it == MODE_NIGHT_YES) NightModeChangesResult.Success(it) else NightModeChangesResult.Success(MODE_NIGHT_FOLLOW_SYSTEM) }
        .onErrorReturn {
            if (it is NullPointerException) { /**pref_theme contains null: doing initial setup... */
                try {
                    with (MODE_NIGHT_FOLLOW_SYSTEM) {
                        sharedPreferences.writeStringOf(context.getString(R.string.pref_theme), this.toString())
                        NightModeChangesResult.Success(this)
                    }
                } catch (e: Throwable) {
                    NightModeChangesResult.SharedChangesCorruptionError
                }
            } else {
                NightModeChangesResult.SharedChangesCorruptionError
            }
        }.onErrorReturn { NightModeChangesResult.SharedChangesCorruptionError }
        .doOnError { logger.e(label = "Problem with changing theme!", stackTrace = it.stackTrace) }
        .compose(baseComposers.commonMaybeFetchTransformer())

    fun checkLocaleChanged(currentLocale: String): Maybe<LocaleChangedResult> = Single.just(currentLocale)
        .filter { sharedPreferences.getStringOf(context.getString(R.string.pref_lang)).equals(it).not() }
        .map<LocaleChangedResult> { LocaleChangedResult.Success }
        .onErrorReturn { LocaleChangedResult.SharedPreferencesCorruptionError }
        .doOnError { logger.e(label = "Problem with locale changes handling!", stackTrace = it.stackTrace) }
}

sealed class NightModeChangesResult {
    data class Success(val code: Int) : NightModeChangesResult()
    object SharedChangesCorruptionError : NightModeChangesResult()
}

sealed class LocaleChangedResult {
    object Success : LocaleChangedResult()
    object SharedPreferencesCorruptionError : LocaleChangedResult()
}
