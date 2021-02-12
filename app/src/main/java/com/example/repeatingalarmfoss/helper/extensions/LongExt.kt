package com.example.repeatingalarmfoss.helper.extensions

import java.text.SimpleDateFormat
import java.util.*

fun minutesToMilliseconds(minutes: Long) = minutes * 1000 * 60
fun hoursToMilliseconds(hours: Long) = hours * 1000 * 60 * 60
fun daysToMilliseconds(days: Long) = days * 1000 * 60 * 60 * 24

fun Long.toReadableDate(): String = SimpleDateFormat(DATE_PATTERN_FOR_LOGGING).format(Date(this))
fun Long.getHoursAndMinutes(separator: String = ":"): String = SimpleDateFormat("HH${separator}mm").format(Date(this))
