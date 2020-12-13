package com.example.repeatingalarmfoss.interactors

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.helper.extensions.LongExt
import com.example.repeatingalarmfoss.helper.extensions.getHoursAndMinutes
import com.example.repeatingalarmfoss.usecases.NextLaunchTimeCalculationUseCase
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar.*

private const val WEDNESDAY_CHOSEN = "00001000"

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class NextLaunchTimeCalculationWeekdaysSetTest {
    private val currentTime = 1605652920000L /*18 Wed Nov 2020 00:41*/
    private val calendar = getInstance().apply { timeInMillis = currentTime }
    private val nextLaunchTimeCalculationUseCaseTest = NextLaunchTimeCalculationUseCase(InstrumentationRegistry.getInstrumentation().context)

    @Test
    fun `same day, greater time`() {
        assert(calendar.get(DAY_OF_WEEK) == WEDNESDAY)
        val alarmsTime = currentTime + LongExt.minutesToMilliseconds(1)
        assertEquals(alarmsTime, nextLaunchTimeCalculationUseCaseTest.getNextLaunchTime(alarmsTime.getHoursAndMinutes(), WEDNESDAY_CHOSEN, calendar))
    }

    @Test
    fun `same day, lesser time`() {
        assert(calendar.get(DAY_OF_WEEK) == WEDNESDAY)
        val alarmsTime = currentTime - LongExt.minutesToMilliseconds(1)
        assertEquals(alarmsTime + LongExt.daysToMilliseconds(7), nextLaunchTimeCalculationUseCaseTest.getNextLaunchTime(alarmsTime.getHoursAndMinutes(), WEDNESDAY_CHOSEN, calendar))
    }

    @Test
    fun `same day, equal time`() {
        assert(calendar.get(DAY_OF_WEEK) == WEDNESDAY)
        assertEquals(currentTime + LongExt.daysToMilliseconds(7), nextLaunchTimeCalculationUseCaseTest.getNextLaunchTime(currentTime.getHoursAndMinutes(), WEDNESDAY_CHOSEN, calendar))
    }
}

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class NextLaunchTimeCalculationIntervalSetTest {
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val alarmsTime = 1605652920000L /*18 Wed Nov 2020 00:41*/
    private val nextLaunchTimeCalculationUseCaseTest = NextLaunchTimeCalculationUseCase(context)

    /*todo improve after reBoot*/
    @Test
    fun `every hour`() = assertEquals(alarmsTime + LongExt.hoursToMilliseconds(1), nextLaunchTimeCalculationUseCaseTest.getNextLaunchTime(alarmsTime, "1${context.getString(R.string.time_unit_hour)}"))

    @Test
    fun `once a week`() = assertEquals(alarmsTime + LongExt.daysToMilliseconds(7), nextLaunchTimeCalculationUseCaseTest.getNextLaunchTime(alarmsTime, "7${context.getString(R.string.time_unit_day)}"))
}