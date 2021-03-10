package com.example.repeatingalarmfoss.interactors

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.helper.extensions.getDefaultSharedPreferences
import com.example.repeatingalarmfoss.helper.extensions.getLocalesLanguage
import com.example.repeatingalarmfoss.helper.extensions.getStringOf
import com.example.repeatingalarmfoss.helper.extensions.provideUpdatedContextWithNewLocale
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val LANGUAGE_ID_ENGLISH = "en"
private const val LANGUAGE_ID_RUSSIAN = "ru"
private const val LANGUAGE_ID_UNSUPPORTED = "de"

/* TODO: ideas for end-to-end tests:
*    First install, supported language presented on a device and translations in the app by default corresponding device language
*      First install, unsupported language presented on a device - application presented in English language
*        In-app language changed via settings and this language persisting throughout reboots and app relaunches (but not reinstalling!)
*          Device-wide language change (via system settings) cause no effect on app-wide language chosen (even if it was initial load by default - see p.1) whether it's supported or not.
* */

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class InAppLanguageChangeTest {
    private var context = InstrumentationRegistry.getInstrumentation().context
    private var sharedPrefs = context.getDefaultSharedPreferences()
    private val langPrefId = context.getString(R.string.pref_lang)

    @Before
    fun setUp() {
        sharedPrefs.edit().putString(langPrefId, null).commit()
    }

    @Test
    fun `applying EN locale on Context`() {
        Assert.assertEquals(sharedPrefs.getString(langPrefId, null), null)
        sharedPrefs.edit().putString(langPrefId, LANGUAGE_ID_ENGLISH).commit()
        Assert.assertEquals(sharedPrefs.getString(langPrefId, null), LANGUAGE_ID_ENGLISH)

        val context = context.provideUpdatedContextWithNewLocale()
        Assert.assertEquals(LANGUAGE_ID_ENGLISH, context.resources.configuration.getLocalesLanguage())
        Assert.assertEquals(LANGUAGE_ID_ENGLISH, sharedPrefs.getStringOf(langPrefId))
    }

    @Test
    fun `applying RU locale on Context`() {
        Assert.assertEquals(sharedPrefs.getString(langPrefId, null), null)
        sharedPrefs.edit().putString(langPrefId, LANGUAGE_ID_RUSSIAN).commit()
        Assert.assertEquals(sharedPrefs.getString(langPrefId, null), LANGUAGE_ID_RUSSIAN)

        val context = context.provideUpdatedContextWithNewLocale(LANGUAGE_ID_RUSSIAN, null)
        Assert.assertEquals(LANGUAGE_ID_RUSSIAN, context.resources.configuration.getLocalesLanguage())
        Assert.assertEquals(LANGUAGE_ID_RUSSIAN, sharedPrefs.getStringOf(langPrefId))
    }

    @Test
    fun `first launch - EN language chosen on device`() {
        val context = context.provideUpdatedContextWithNewLocale(null, LANGUAGE_ID_ENGLISH)
        Assert.assertEquals(LANGUAGE_ID_ENGLISH, context.resources.configuration.getLocalesLanguage())
        Assert.assertEquals(LANGUAGE_ID_ENGLISH, sharedPrefs.getStringOf(context.getString(R.string.pref_lang)))
    }

    @Test
    fun `first launch - unsupported language chosen on device`() {
        val context = context.provideUpdatedContextWithNewLocale(null, LANGUAGE_ID_UNSUPPORTED)
        Assert.assertEquals(LANGUAGE_ID_ENGLISH, context.resources.configuration.getLocalesLanguage())
        Assert.assertEquals(LANGUAGE_ID_ENGLISH, sharedPrefs.getStringOf(context.getString(R.string.pref_lang)))
    }

    @Test
    fun `first launch - RU language (supported) chosen on device`() {
        val context = context.provideUpdatedContextWithNewLocale(null, LANGUAGE_ID_RUSSIAN)
        Assert.assertEquals(LANGUAGE_ID_RUSSIAN, context.resources.configuration.getLocalesLanguage())
        Assert.assertEquals(LANGUAGE_ID_RUSSIAN, sharedPrefs.getStringOf(context.getString(R.string.pref_lang)))
    }
}