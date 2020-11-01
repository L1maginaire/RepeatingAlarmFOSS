package com.example.repeatingalarmfoss.screens.added_tasks

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment(private val onTimeSetCallback: OnTimeSetCallback) : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minutes: Int) = onTimeSetCallback.onTimeSet(hourOfDay, minutes)

    interface OnTimeSetCallback{
        fun onTimeSet(hourOfDay: Int, minutes: Int)
    }
}