@file:Suppress("PropertyName")

package com.example.repeatingalarmfoss.helper

import android.util.Log
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/*TODO: current thread mark, encryption*/
@Singleton
class FlightRecorder constructor(private val logStorage: File) {
    var TAPE_VOLUME = 10 * 1024 * 1024 /** 10 MB **/

    fun i(toPrintInLogcat: Boolean = false, what: () -> String) = clearBeginningIfNeeded("} I {", what).also { logStorage.appendText("} I { ${what.invoke()}\n") }.also { if(toPrintInLogcat) { Log.i(this::class.java.simpleName, what.invoke())} }
    fun d(toPrintInLogcat: Boolean = false, what: () -> String) = clearBeginningIfNeeded("} D {", what).also { logStorage.appendText("} D { ${what.invoke()}\n") }.also { if(toPrintInLogcat) { Log.i(this::class.java.simpleName, what.invoke())} }
    fun w(toPrintInLogcat: Boolean = false, what: () -> String) = clearBeginningIfNeeded("} W {", what).also { logStorage.appendText("} W { ${what.invoke()}\n") }.also { if(toPrintInLogcat) { Log.i(this::class.java.simpleName, what.invoke())} }
    fun e(toPrintInLogcat: Boolean = false, what: () -> String) = clearBeginningIfNeeded("} E {", what).also { logStorage.appendText("} E { ${what.invoke()}\n") }.also { if(toPrintInLogcat) { Log.i(this::class.java.simpleName, what.invoke())} }
    fun wtf(toPrintInLogcat: Boolean = false, what: () -> String) = clearBeginningIfNeeded("} X {", what).also { logStorage.appendText("} X { ${what.invoke()}\n") }.also { if(toPrintInLogcat) { Log.i(this::class.java.simpleName, what.invoke())} }

    private fun clearBeginningIfNeeded(meta: String, what: () -> String) {
        val newDataSize = "$meta ${what.invoke()}\n".toByteArray().size
        if ((logStorage.length() + newDataSize.toLong()) > TAPE_VOLUME) {
            val dataToRemain = logStorage.readBytes().drop(newDataSize).toByteArray()
            logStorage.writeBytes(dataToRemain)
        }
    }
}