package com.example.repeatingalarmfoss.helper.extensions

import android.content.SharedPreferences

const val PREF_APP_LANG = "PREF_APP_LANG"
const val PREF_APP_THEME = "PREF_APP_THEME"

fun SharedPreferences.getBooleanOf(keyToValue: String) = getBoolean(keyToValue, false)
fun SharedPreferences.getStringOf(keyToValue: String) = getString(keyToValue, null)
fun SharedPreferences.writeBooleanOf(keyToValue: String, value: Boolean) = edit().also { it.putBoolean(keyToValue, value) }.apply()
fun SharedPreferences.writeStringOf(keyToValue: String, value: String) = edit().also { it.putString(keyToValue, value) }.apply()