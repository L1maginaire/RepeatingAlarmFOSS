package com.example.repeatingalarmfoss.usecases

import android.content.Context
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.helper.FixedSizeBitSet
import com.example.repeatingalarmfoss.helper.extensions.LongExt.daysToMilliseconds
import com.example.repeatingalarmfoss.helper.extensions.LongExt.hoursToMilliseconds
import com.example.repeatingalarmfoss.helper.extensions.LongExt.minutesToMilliseconds
import com.example.repeatingalarmfoss.screens.added_tasks.AMOUNT_DAYS_IN_WEEK
import java.util.*
import javax.inject.Inject

class NextLaunchTimeCalculationUseCase @Inject constructor(private val appContext: Context) {
    /** @param time - Timestamp, implies hours (in 24-hour format) and minutes divided with separator ":". For example, 21:12

     *  @param chosenWeekDaysBinaryString - String, denoting weekdays "chosen" or not in binary format, (zero index is redundant), first index is Sunday, second is Monday, etc.
    For example, argument value is: 00100001. This means, "chosen" days are Monday and Saturday

     *  @return - Timestamp in format of milliseconds, denoting time in future.
     * */
    fun getNextLaunchTime(time: String, chosenWeekDaysBinaryString: String, calendar: Calendar = Calendar.getInstance()): Long {
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val chosenWeekDays = FixedSizeBitSet.fromBinaryString(chosenWeekDaysBinaryString)
        val hours = time.split(":")[0].toInt()
        val minutes = time.split(":")[1].toInt()
        return calendar.apply {
            if ((chosenWeekDays.get(today) && hours > calendar.get(Calendar.HOUR_OF_DAY)) || (chosenWeekDays.get(today) && hours == calendar.get(Calendar.HOUR_OF_DAY) && minutes > calendar.get(Calendar.MINUTE))) {
                set(Calendar.DAY_OF_WEEK, today)
            } else {
                if (chosenWeekDays.getChosenIndices().size == 1) {
                    set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + AMOUNT_DAYS_IN_WEEK)
                } else {
                    set(Calendar.DAY_OF_WEEK, chosenWeekDays.getNextIndex(today))
                }
            }
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Suppress("MoveVariableDeclarationIntoWhen")
            /** @param repeatingClassifierValue must consist of number and one of 3 classifiers (Minutes, Hours, Days) without delimiter*/
    fun getNextLaunchTime(launchTime: Long, repeatingClassifierValue: String): Long {
        val interval = Integer.parseInt(repeatingClassifierValue.replace("[^0-9]".toRegex(), ""))
        val classifier = repeatingClassifierValue.replace("\\d+".toRegex(), "")
        return when (classifier) {
            appContext.resources.getString(R.string.time_unit_minute) -> launchTime + minutesToMilliseconds(interval.toLong())
            appContext.resources.getString(R.string.time_unit_hour) -> launchTime + hoursToMilliseconds(interval.toLong())
            appContext.resources.getString(R.string.time_unit_day) -> launchTime + daysToMilliseconds(interval.toLong())
            else -> throw IllegalArgumentException()
        }
    }
}
