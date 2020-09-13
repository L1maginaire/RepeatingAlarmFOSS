package com.example.repeatingalarmfoss.helper.extensions

fun String.toLowerCase() = this.toLowerCase(java.util.Locale.getDefault())

fun String.yellow() = this.toColorfulString(33)
fun String.red() = this.toColorfulString(31)

private fun String.toColorfulString(colorCode: Int): String = 27.toChar() + "[${colorCode}m$this" + 27.toChar() + "[0m"
