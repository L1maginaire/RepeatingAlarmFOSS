@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.example.repeatingalarmfoss.helper.extensions

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Throws(ParseException::class)
        /** all the arguments should be presented in such format: "HH:mm", where HH is 24-hour format hours and mm are minutes */
fun isTimeBetweenTwoTime(startThreshold: String, endThreshold: String, verifiableTime: String): Boolean {
    val reg = "^([0-1][0-9]|2[0-3]):([0-5][0-9])$"
    if (startThreshold.matches(reg.toRegex()) && endThreshold.matches(reg.toRegex()) && verifiableTime.matches(reg.toRegex())) {
        val start: Calendar = Calendar.getInstance().apply { time = SimpleDateFormat(TIME_PATTERN_HOURS_24_MINUTES, Locale.UK).parse(startThreshold) }

        val end: Calendar = Calendar.getInstance().apply { time = SimpleDateFormat(TIME_PATTERN_HOURS_24_MINUTES, Locale.UK).parse(verifiableTime) }

        val verifiable: Calendar = Calendar.getInstance().apply { time = SimpleDateFormat(TIME_PATTERN_HOURS_24_MINUTES, Locale.UK).parse(endThreshold) }

        if (endThreshold < startThreshold) {
            verifiable.add(Calendar.DATE, 1)
            end.add(Calendar.DATE, 1)
        }
        return (end.time.after(start.time) || end.time.compareTo(start.time) == 0) && end.time.before(verifiable.time)
    } else throw IllegalArgumentException()
}
