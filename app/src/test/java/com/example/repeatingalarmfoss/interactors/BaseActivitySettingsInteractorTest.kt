package com.example.repeatingalarmfoss.interactors

import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.platform.app.InstrumentationRegistry
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.*
import com.example.repeatingalarmfoss.helper.rx.BaseComposers
import com.example.repeatingalarmfoss.helper.rx.TestSchedulers
import com.example.repeatingalarmfoss.usecases.BaseActivitySettingsInteractor
import com.example.repeatingalarmfoss.usecases.LocaleChangedResult
import com.example.repeatingalarmfoss.usecases.NightModeChangesResult
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val YES = AppCompatDelegate.MODE_NIGHT_YES
private const val RUSSIAN_LOCALE = "ru"

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class BaseActivitySettingsInteractorTest {
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val themePrefId = context.getString(R.string.pref_theme)
    private val langPrefId = context.getString(R.string.pref_lang)
    private val sharedPrefs = context.getDefaultSharedPreferences()
    private val logger = FlightRecorder(createTempFile())
    private val nightModePreferenceInteractor = BaseActivitySettingsInteractor(sharedPrefs, BaseComposers(TestSchedulers(), logger), logger, context)

    @Test
    fun `if no value persisted, write default -- MODE_NIGHT_FOLLOW_SYSTEM -- and recreate`() {
        assert(sharedPrefs.getStringOf(themePrefId) == null)
        nightModePreferenceInteractor.handleThemeChanges(YES).test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(NightModeChangesResult.Success(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
        assert(sharedPrefs.getStringOf(themePrefId) == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString())
    }

    @Test
    fun `NightModeValue -- if the same value persisted, return no result`() {
        sharedPrefs.writeStringOf(themePrefId, YES.toString())
        assert(sharedPrefs.getStringOf(themePrefId)!!.toInt() == YES)
        nightModePreferenceInteractor.handleThemeChanges(YES).test()
            .assertNoErrors().assertComplete()
            .assertNoValues()
        assert(sharedPrefs.getStringOf(themePrefId) == YES.toString())
    }

    @Test
    fun `if something is wrong with SharedPreferences, return NightModePreferencesResult # SharedPreferencesCorruptionError`() {
        val sharedPrefMock = mock(SharedPreferences::class.java)
        `when`(sharedPrefMock.getStringOf(themePrefId)).thenThrow(RuntimeException::class.java)
        val nightModePreferenceInteractor = BaseActivitySettingsInteractor(sharedPrefMock, BaseComposers(TestSchedulers(), logger), logger, context)
        nightModePreferenceInteractor.handleThemeChanges(YES).test()
            .assertNoErrors()
            .assertComplete()
            .assertResult(NightModeChangesResult.SharedChangesCorruptionError)
    }

    @Test
    fun `if something is wrong with SharedPreferences, return LocaleChangedResult # SharedPreferencesCorruptionError`() {
        val sharedPrefMock = mock(SharedPreferences::class.java)
        `when`(sharedPrefMock.getStringOf(langPrefId)).thenThrow(RuntimeException::class.java)
        val nightModePreferenceInteractor = BaseActivitySettingsInteractor(sharedPrefMock, BaseComposers(TestSchedulers(), logger), logger, context)
        nightModePreferenceInteractor.checkLocaleChanged(RUSSIAN_LOCALE).test()
            .assertNoErrors()
            .assertComplete()
            .assertResult(LocaleChangedResult.SharedPreferencesCorruptionError)
    }

    @Test
    fun `Language -- if the same value persisted, return no result`() {
        sharedPrefs.writeStringOf(langPrefId, RUSSIAN_LOCALE)
        assert(sharedPrefs.getStringOf(langPrefId) == RUSSIAN_LOCALE)
        nightModePreferenceInteractor.checkLocaleChanged(RUSSIAN_LOCALE).test()
            .assertNoErrors()
            .assertComplete()
            .assertNoValues()
        assert(sharedPrefs.getStringOf(langPrefId) == RUSSIAN_LOCALE)
    }
}