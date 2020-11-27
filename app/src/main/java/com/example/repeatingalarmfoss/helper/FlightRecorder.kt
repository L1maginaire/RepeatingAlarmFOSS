@file:Suppress("PropertyName")

package com.example.repeatingalarmfoss.helper

import android.util.Log
import com.example.repeatingalarmfoss.BuildConfig
import com.example.repeatingalarmfoss.helper.extensions.DATE_PATTERN_FOR_LOGGING
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton

/*TODO: current thread mark, encryption*/
@Singleton
class FlightRecorder constructor(private val logStorage: File) {
    private val isDebug = BuildConfig.DEBUG
    var TAPE_VOLUME = 10 * 1024 * 1024 /** 10 MB **/

    /**
     * @param when - time in milliseconds
     * */
    fun logScheduledEvent(toPrintInLogcat: Boolean = true, what: () -> String, `when`: Long) {
        val message = { what.invoke() + " " + SimpleDateFormat(DATE_PATTERN_FOR_LOGGING, Locale.UK).format(`when`) }
        clearBeginningIfNeeded("} I {", message)
            .also { logStorage.appendText("} I { ${message.invoke()}\n") }
            .also { if(toPrintInLogcat && isDebug) { Log.i(this::class.java.simpleName, message.invoke())} }
    }

    fun i(toPrintInLogcat: Boolean = true, what: () -> String) = clearBeginningIfNeeded("} I {", what)
        .also { logStorage.appendText("} I { ${what.invoke()}\n") }
        .also { if(toPrintInLogcat && isDebug) { Log.i(this::class.java.simpleName, what.invoke())} }

    fun d(toPrintInLogcat: Boolean = true, what: () -> String) = clearBeginningIfNeeded("} D {", what)
        .also { logStorage.appendText("} D { ${what.invoke()}\n") }
        .also { if(toPrintInLogcat && isDebug) { Log.i(this::class.java.simpleName, what.invoke())} }

    fun w(toPrintInLogcat: Boolean = true, what: () -> String) = clearBeginningIfNeeded("} W {", what)
        .also { logStorage.appendText("} W { ${what.invoke()}\n") }
        .also { if(toPrintInLogcat && isDebug) { Log.i(this::class.java.simpleName, what.invoke())} }

    fun e(toPrintInLogcat: Boolean = true, stackTrace: Array<StackTraceElement>) {
        val readableStackTrace = stackTrace.joinToString(separator = "\n") { it.toString() }
        clearBeginningIfNeeded("} E {") { readableStackTrace }
            .also { logStorage.appendText("} E { $readableStackTrace\n") }
            .also { if(toPrintInLogcat && isDebug) { Log.i(this::class.java.simpleName, readableStackTrace)} }
    }

    fun wtf(toPrintInLogcat: Boolean = true, what: () -> String) = clearBeginningIfNeeded("} X {", what)
        .also { logStorage.appendText("} X { ${what.invoke()}\n") }
        .also { if(toPrintInLogcat && isDebug) { Log.i(this::class.java.simpleName, what.invoke())} }

    fun getEntireRecord() = try { logStorage.readText() } catch (e: FileNotFoundException) {
        logStorage.createNewFile()
        logStorage.readText()
    }
    
    fun clear() = logStorage.writeText("")

    private fun clearBeginningIfNeeded(meta: String, what: () -> String) {
        val newDataSize = "$meta ${what.invoke()}\n".toByteArray().size
        if ((logStorage.length() + newDataSize.toLong()) > TAPE_VOLUME) {
            val dataToRemain = logStorage.readBytes().drop(newDataSize).toByteArray()
            logStorage.writeBytes(dataToRemain)
        }
    }
}