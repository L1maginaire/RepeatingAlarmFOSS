package com.example.repeatingalarmfoss

import org.junit.Assert.assertFalse
import org.junit.Test

class IsTimeInRangeTest {
    @Test
    fun isTimeBetweenTwoTime() {
        assert(com.example.repeatingalarmfoss.helper.extensions.isTimeBetweenTwoTime("07:00", "17:30", "15:30"))
        assertFalse(com.example.repeatingalarmfoss.helper.extensions.isTimeBetweenTwoTime("17:00", "21:30", "16:30"))
        assert(com.example.repeatingalarmfoss.helper.extensions.isTimeBetweenTwoTime("23:00", "04:00", "02:00"))
        assertFalse(com.example.repeatingalarmfoss.helper.extensions.isTimeBetweenTwoTime("00:30", "06:00", "06:00"))
        assert(com.example.repeatingalarmfoss.helper.extensions.isTimeBetweenTwoTime("00:00", "09:00", "00:00"))
        assert(com.example.repeatingalarmfoss.helper.extensions.isTimeBetweenTwoTime("00:00", "09:00", "08:59"))
        assertFalse(com.example.repeatingalarmfoss.helper.extensions.isTimeBetweenTwoTime("00:00", "09:00", "09:00"))
    }
}
