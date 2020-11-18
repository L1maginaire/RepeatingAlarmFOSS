package com.example.repeatingalarmfoss.helper.extensions

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

/** Represents date in format "day_of_month concise_month_name year"
 *  For example:
 *  23 Dec 2014
 *  01 May 2020
 * */
const val DATE_PATTERN_DAY_MONTH_YEAR = "dd MMM yyyy"


fun String.toLowerCase() = this.toLowerCase(java.util.Locale.getDefault())
fun String.yellow() = this.toColorfulString(33)

fun String.red() = this.toColorfulString(31)

private fun String.toColorfulString(colorCode: Int): String = 27.toChar() + "[${colorCode}m$this" + 27.toChar() + "[0m"
