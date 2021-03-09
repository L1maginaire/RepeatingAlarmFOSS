package com.example.repeatingalarmfoss.interactors

import android.content.SharedPreferences
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.getDefaultSharedPreferences
import com.example.repeatingalarmfoss.helper.extensions.getStringOf
import com.example.repeatingalarmfoss.helper.extensions.writeStringOf
import com.example.repeatingalarmfoss.helper.rx.BaseComposers
import com.example.repeatingalarmfoss.helper.rx.TestSchedulers
import com.example.repeatingalarmfoss.usecases.BaseActivitySettingsInteractor
import com.example.repeatingalarmfoss.usecases.LocaleChangedResult
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RUSSIAN_LOCALE = "ru"

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class InAppLanguageChangeTest {
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val sharedPrefs = context.getDefaultSharedPreferences()
    private val logger = FlightRecorder(createTempFile())
    private val nightModePreferenceInteractor = BaseActivitySettingsInteractor(sharedPrefs, BaseComposers(TestSchedulers(), logger), logger, context)
    private val langPrefId = context.getString(R.string.pref_lang)

    @Test
    fun `if something is wrong with SharedPreferences, return LocaleChangedResult # SharedPreferencesCorruptionError`() {
        val sharedPrefMock = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPrefMock.getStringOf(langPrefId)).thenThrow(RuntimeException::class.java)
        val nightModePreferenceInteractor = BaseActivitySettingsInteractor(sharedPrefMock, BaseComposers(TestSchedulers(), logger), logger, context)

        nightModePreferenceInteractor.checkLocaleChanged(RUSSIAN_LOCALE)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertResult(LocaleChangedResult.SharedPreferencesCorruptionError)
    }

    @Test
    fun `if the same value persisted, return no result`() {
        sharedPrefs.writeStringOf(langPrefId, RUSSIAN_LOCALE)
        assert(sharedPrefs.getStringOf(langPrefId) == RUSSIAN_LOCALE)

        nightModePreferenceInteractor.checkLocaleChanged(RUSSIAN_LOCALE)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertNoValues()
        assert(sharedPrefs.getStringOf(langPrefId) == RUSSIAN_LOCALE)
    }

    @Test
    fun `if another one language persisted, trigger recreating and applying new `() {
        /*TODO test functionality with applying new language (of app setting) */
    }
}