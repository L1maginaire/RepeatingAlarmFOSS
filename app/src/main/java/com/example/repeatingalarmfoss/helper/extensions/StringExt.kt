package com.example.repeatingalarmfoss.helper.extensions

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt
import java.text.SimpleDateFormat
import java.util.*

/** Represents hours and minutes in hours:minutes way. Hours will be shown in 24-hour format. For example,
 *  00:12
 *  05:55
 *  22:00
 *  */
const val TIME_PATTERN_HOURS_24_MINUTES = "HH:mm"

/** Represents date and in such format: "day_of_month concise_month_name year 24_format_hours:minutes"
 *  For example:
 *  23 Dec 2014 00:12
 *  01 May 2020 05:55
 *  */
const val DATE_PATTERN_FOR_LOGGING = "dd EEE MMM yyyy HH:mm"
const val DATE_PATTERN_FOR_LOGGING2 = "dd MMM yyyy HH:mm"

/** Represents date in format "day_of_month concise_month_name year"
 *  For example:
 *  23 Dec 2014
 *  01 May 2020
 * */
const val DATE_PATTERN_DAY_MONTH_YEAR = "dd MMM yyyy"


fun String.toLowerCase() = this.toLowerCase(Locale.getDefault())
fun String.yellow() = 27.toChar() + "[33m$this" + 27.toChar() + "[0m"
fun String.red() = 27.toChar() + "[31m$this" + 27.toChar() + "[0m"

/** String representing current hours in 24 format and minutes, with `:` delimiter. For example:
 * 00:52
 * 08:11
 * */
fun now(): String = SimpleDateFormat(TIME_PATTERN_HOURS_24_MINUTES, Locale.getDefault()).format(Date())

fun today(): String = SimpleDateFormat(DATE_PATTERN_DAY_MONTH_YEAR, Locale.getDefault()).format(Date())

fun String.toColorfulString(@ColorInt color: Int) = SpannableString(this).apply { setSpan(ForegroundColorSpan(color), 0, length, 0) }