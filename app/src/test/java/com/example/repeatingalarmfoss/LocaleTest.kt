package com.example.repeatingalarmfoss

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.example.repeatingalarmfoss.helper.extensions.getLocalesLanguage
import com.example.repeatingalarmfoss.helper.extensions.provideUpdatedContextWithNewLocale
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val LANGUAGE_ID_ENGLISH = "en"
private const val LANGUAGE_ID_RUSSIAN = "ru"

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class LocaleTest {
    private val context = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun `extension function creates context with new appropriate locale`() {
        val context1 = context.provideUpdatedContextWithNewLocale(LANGUAGE_ID_ENGLISH, null)
        Assert.assertEquals(LANGUAGE_ID_ENGLISH, context1.resources.configuration.getLocalesLanguage())

        val context2 = context.provideUpdatedContextWithNewLocale("zh_TW_#Hant-x-java", null)
        Assert.assertEquals(LANGUAGE_ID_ENGLISH, context2.resources.configuration.getLocalesLanguage())

        val context3 = context.provideUpdatedContextWithNewLocale(LANGUAGE_ID_RUSSIAN, null)
        Assert.assertEquals(LANGUAGE_ID_RUSSIAN, context3.resources.configuration.getLocalesLanguage())
    }
}