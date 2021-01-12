package com.example.repeatingalarmfoss.interactors

import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.platform.app.InstrumentationRegistry
import com.example.repeatingalarmfoss.base.LocaleChangedResult
import com.example.repeatingalarmfoss.base.BaseActivitySettingsInteractor
import com.example.repeatingalarmfoss.base.NightModeChangesResult
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.*
import com.example.repeatingalarmfoss.helper.rx.BaseComposers
import com.example.repeatingalarmfoss.helper.rx.TestSchedulers
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
    private val sharedPrefs = context.getDefaultSharedPreferences()
    private val logger = FlightRecorder(createTempFile())
    private val nightModePreferenceInteractor = BaseActivitySettingsInteractor(sharedPrefs, BaseComposers(TestSchedulers(), logger), logger)

    @Test
    fun `if no value persisted, write default -- AppCompatDelegate # getDefaultNightMode() -- and recreate`() {
        assert(sharedPrefs.getStringOf(PREF_APP_THEME) == null)
        nightModePreferenceInteractor.handleThemeChanges(YES).test().assertComplete().assertNoErrors().assertResult(NightModeChangesResult.Success(YES))
        assert(sharedPrefs.getStringOf(PREF_APP_THEME) == YES.toString())
    }

    @Test
    fun `NightModeValue -- if the same value persisted, return no result`() {
        sharedPrefs.writeStringOf(PREF_APP_THEME, YES.toString())
        assert(sharedPrefs.getStringOf(PREF_APP_THEME)!!.toInt() == YES)
        nightModePreferenceInteractor.handleThemeChanges(YES).test().assertNoErrors().assertComplete().assertNoValues()
        assert(sharedPrefs.getStringOf(PREF_APP_THEME) == YES.toString())
    }

    @Test
    fun `if something is wrong with SharedPreferences, return NightModePreferencesResult # SharedPreferencesCorruptionError`() {
        val sharedPrefMock = mock(SharedPreferences::class.java)
        `when`(sharedPrefMock.getStringOf(PREF_APP_THEME)).thenThrow(RuntimeException::class.java)
        val nightModePreferenceInteractor = BaseActivitySettingsInteractor(sharedPrefMock, BaseComposers(TestSchedulers(), logger), logger)
        nightModePreferenceInteractor.handleThemeChanges(YES).test().assertNoErrors().assertComplete().assertResult(NightModeChangesResult.SharedChangesCorruptionError)
    }

    @Test
    fun `if something is wrong with SharedPreferences, return LocaleChangedResult # SharedPreferencesCorruptionError`() {
        val sharedPrefMock = mock(SharedPreferences::class.java)
        `when`(sharedPrefMock.getStringOf(PREF_APP_LANG)).thenThrow(RuntimeException::class.java)
        val nightModePreferenceInteractor = BaseActivitySettingsInteractor(sharedPrefMock, BaseComposers(TestSchedulers(), logger), logger)
        nightModePreferenceInteractor.checkLocaleChanged(RUSSIAN_LOCALE).test().assertNoErrors().assertComplete().assertResult(LocaleChangedResult.SharedPreferencesCorruptionError)
    }

    @Test
    fun `Language -- if the same value persisted, return no result`() {
        sharedPrefs.writeStringOf(PREF_APP_LANG, RUSSIAN_LOCALE)
        assert(sharedPrefs.getStringOf(PREF_APP_LANG) == RUSSIAN_LOCALE)
        nightModePreferenceInteractor.checkLocaleChanged(RUSSIAN_LOCALE).test().assertNoErrors().assertComplete().assertNoValues()
        assert(sharedPrefs.getStringOf(PREF_APP_LANG) == RUSSIAN_LOCALE)
    }

}