package com.example.repeatingalarmfoss.db

import androidx.room.TypeConverter

enum class RepeatingClassifier {
    DAY_OF_WEEK, DAY_OF_MONTH, EVERY_X_TIME_UNIT
}

enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

class RepeatingClassifierConverter {
    @TypeConverter
    fun toRepeatingClassifier(value: String): RepeatingClassifier = enumValueOf(value)

    @TypeConverter
    fun fromRepeatingClassifier(repeatingClassifier: RepeatingClassifier): String = repeatingClassifier.name
}