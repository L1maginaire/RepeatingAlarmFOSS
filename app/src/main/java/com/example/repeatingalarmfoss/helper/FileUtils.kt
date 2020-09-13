package com.example.repeatingalarmfoss.helper

import android.content.Context
import java.io.File

private const val DEBUG_LOGS_DIR = "FlightRecordings"
private const val DEBUG_LOGS_STORAGE_FILE_NAME = "tape.log"

object FileUtils {
    fun getFlightRecorderTape(context: Context): File = File(getFileDir(context.filesDir, DEBUG_LOGS_DIR), DEBUG_LOGS_STORAGE_FILE_NAME)

    private fun getFileDir(root: File, directory: String): File = File(root, directory).apply {
        if (exists().not()) {
            mkdir()
        }
    }
}