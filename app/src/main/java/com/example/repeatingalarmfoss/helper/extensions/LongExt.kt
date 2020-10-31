package com.example.repeatingalarmfoss.helper.extensions

object LongExt {
    fun minutesToMilliseconds(minutes: Long) = minutes*1000*60
    fun hoursToMilliseconds(hours: Long) = hours*1000*60*60
    fun daysToMilliseconds(days: Long) = days*1000*60*60*24
}