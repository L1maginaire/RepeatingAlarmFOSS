package com.example.repeatingalarmfoss.helper.extensions

import android.content.SharedPreferences

const val PREF_LOW_BATTERY_DND_AT_NIGHT = "PREF_LOW_BATTERY_DND_AT_NIGHT"
const val PREF_LAUNCH_COUNTER = "PREF_LAUNCH_COUNTER"
const val PREF_NEVER_SHOW_RATE_APP = "PREF_LAUNCH_NEVER_SHOW"

fun SharedPreferences.getBooleanOf(keyToValue: String) = getBoolean(keyToValue, false)
fun SharedPreferences.getStringOf(keyToValue: String) = getString(keyToValue, null)
fun SharedPreferences.writeBooleanOf(keyToValue: String, value: Boolean) = edit().also { it.putBoolean(keyToValue, value) }.apply()
fun SharedPreferences.writeStringOf(keyToValue: String, value: String) = edit().also { it.putString(keyToValue, value) }.apply()
fun SharedPreferences.incrementAppLaunchCounter() = edit().also { it.putInt(PREF_LAUNCH_COUNTER, getInt(PREF_LAUNCH_COUNTER, 0).inc()) }.apply()
fun SharedPreferences.getAppLaunchCounter() = getInt(PREF_LAUNCH_COUNTER, 0)