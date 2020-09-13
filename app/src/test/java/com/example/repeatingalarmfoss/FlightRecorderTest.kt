package com.example.repeatingalarmfoss

import android.content.Context
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.yellow
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class DebugLogWritingTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val logFile = createTempFile()
    private val flightRecorder = FlightRecorder.getInstance(context, logFile)

    /** Pay attention to meta wrappers' pattern of
     * @see FlightRecorder,
     * it can be changed any time. if so, change byte size of meta to make it equal.*/
    private val metaDataSize = "} I { \n".toByteArray().size

    @Test
    fun overwriting() {
        val stringToLog = """
            log_string_01
            log_string_02
            log_string_03
            log_string_04
            log_string_05
            log_string_06
            log_string_07
            log_string_08
            log_string_09
            log_string_10
            log_string_11
            log_string_12
            log_string_13
            log_string_14
            log_string_15
            log_string_16
        """.trimIndent()
        val initialLoadSize = stringToLog.toByteArray().size
        println("Initial string consist of $initialLoadSize bytes".yellow())
        println()
        flightRecorder.TAPE_VOLUME = initialLoadSize + metaDataSize

        flightRecorder.i { stringToLog }
        assertEquals(logFile.length(), (initialLoadSize + metaDataSize).toLong())
        println("Initial text in file:".yellow())
        println(logFile.readText())
        println()

        val newLine = "______________________________________________SOME_LARGE_AMOUNT_OF_TEXT____________________________________________"
        flightRecorder.i { newLine }
        assertEquals(logFile.length(), (initialLoadSize + metaDataSize).toLong())
        println("Text in file after overwriting:".yellow())
        println(logFile.readText())
    }
}