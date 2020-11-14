package com.example.repeatingalarmfoss.screens.added_tasks

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.helper.DEFAULT_UI_SKIP_DURATION
import com.example.repeatingalarmfoss.helper.FixedSizeBitSet
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.DATE_PATTERN_DAY_MONTH_YEAR
import com.example.repeatingalarmfoss.helper.extensions.DATE_PATTERN_FOR_LOGGING
import com.example.repeatingalarmfoss.helper.extensions.TIME_PATTERN_HOURS_24_MINUTES
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.functions.Function7
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.dialog_creating_task.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val AMOUNT_DAYS_IN_WEEK = 7

class SetupAddingTaskFragment : DialogFragment(), TimePickerFragment.OnTimeSetCallback, DatePickerFragment.OnDateSetCallback {
    @Inject
    lateinit var logger: FlightRecorder
    private val clicks = CompositeDisposable()
    private val chosenWeekDays = FixedSizeBitSet(AMOUNT_DAYS_IN_WEEK)
    private lateinit var customView: View
    private lateinit var timeSettingCallback: TimeSettingCallback

    companion object {
        fun newInstance(timeSettingCallback: TimeSettingCallback) = SetupAddingTaskFragment().apply {
            this.timeSettingCallback = timeSettingCallback
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = customView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        buttonOk.isVisible = dialog == null
        if (dialog == null) {
            buttonTimePicker.text = SimpleDateFormat(TIME_PATTERN_HOURS_24_MINUTES, Locale.getDefault()).format(Date())
            buttonDatePicker.text = SimpleDateFormat(DATE_PATTERN_DAY_MONTH_YEAR, Locale.getDefault()).format(Date())
            setupClicks()
            clicks += buttonOk.clicks()
                .throttleFirst(DEFAULT_UI_SKIP_DURATION, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe {
                    buttonOk.isEnabled = false
                    onOkButtonClicked()
                }
        }
    }

    fun setFieldsDefault() {
        buttonTimePicker.text = SimpleDateFormat(TIME_PATTERN_HOURS_24_MINUTES, Locale.getDefault()).format(Date())
        buttonDatePicker.text = SimpleDateFormat(DATE_PATTERN_DAY_MONTH_YEAR, Locale.getDefault()).format(Date())
        toggleMon.isChecked = false
        toggleTue.isChecked = false
        toggleWed.isChecked = false
        toggleThu.isChecked = false
        toggleFri.isChecked = false
        toggleSat.isChecked = false
        toggleSun.isChecked = false
        etTaskDescription.setText("")
        etTimeUnitValue.setText("1")
    }

    override fun onAttach(context: Context) = (requireActivity().application as RepeatingAlarmApp).appComponent.inject(this)
        .apply { super.onAttach(context) }
        .also { customView = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_creating_task, null as ViewGroup?) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog.Builder(requireActivity())
        .setTitle(getString(R.string.add_new_task))
        .setView(customView)
        .setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()
            onOkButtonClicked()
        }
        .setNegativeButton(android.R.string.cancel, null)
        .create().apply {
            setOnShowListener {
                buttonTimePicker.text = SimpleDateFormat(TIME_PATTERN_HOURS_24_MINUTES, Locale.getDefault()).format(Date())
                buttonDatePicker.text = SimpleDateFormat(DATE_PATTERN_DAY_MONTH_YEAR, Locale.getDefault()).format(Date())
                setupClicks()
            }
        }

    private fun onOkButtonClicked() {
        val description = etTaskDescription.text.toString()
        val time = buttonTimePicker.text.toString()
        val chosenInitialDateAndTime: Date? = SimpleDateFormat(DATE_PATTERN_FOR_LOGGING, Locale.getDefault()).apply { isLenient = false }.parse(buttonDatePicker.text.toString() + " " + buttonTimePicker.text.toString())
        when {
            rbDayOfWeek.isChecked -> {
                timeSettingCallback.onTimeSet(description, RepeatingClassifier.DAY_OF_WEEK, chosenWeekDays.toString(), time)
                logger.d(true) { "chosen week days in dialog: $chosenWeekDays" }
            }
            rbXTimeUnit.isChecked -> {
                val currentSpinnerValue = spinnerTimeUnits.selectedItem.toString()
                val repeatingClassifierValue = etTimeUnitValue.text
                logger.d(true) { "chosen date in dialog: $chosenInitialDateAndTime" }
                timeSettingCallback.onTimeSet(description, RepeatingClassifier.EVERY_X_TIME_UNIT, repeatingClassifierValue.toString() + currentSpinnerValue, chosenInitialDateAndTime?.time.toString())
            }
        }
    }

    private fun setupClicks() {
        clicks += buttonTimePicker.clicks()
            .throttleFirst(DEFAULT_UI_SKIP_DURATION, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe { TimePickerFragment(this).show(requireActivity().supportFragmentManager, TimePickerFragment::class.java.simpleName) }

        clicks += buttonDatePicker.clicks()
            .throttleFirst(DEFAULT_UI_SKIP_DURATION, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe { DatePickerFragment(this).show(requireActivity().supportFragmentManager, TimePickerFragment::class.java.simpleName) }

        clicks += Observable.combineLatest(toggleMon.checkedChanges(), toggleTue.checkedChanges(), toggleWed.checkedChanges(), toggleThu.checkedChanges(), toggleFri.checkedChanges(), toggleSat.checkedChanges(), toggleSun.checkedChanges(),
            Function7<Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Unit> { mon, tue, wed, thu, fri, sat, sun ->
                chosenWeekDays.apply {
                    if (mon) set(Calendar.MONDAY) else clear(Calendar.MONDAY)
                    if (tue) set(Calendar.TUESDAY) else clear(Calendar.TUESDAY)
                    if (wed) set(Calendar.WEDNESDAY) else clear(Calendar.WEDNESDAY)
                    if (thu) set(Calendar.THURSDAY) else clear(Calendar.THURSDAY)
                    if (fri) set(Calendar.FRIDAY) else clear(Calendar.FRIDAY)
                    if (sat) set(Calendar.SATURDAY) else clear(Calendar.SATURDAY)
                    if (sun) set(Calendar.SUNDAY) else clear(Calendar.SUNDAY)
                }
            }).subscribe()

        clicks += Observable.combineLatest(buttonDatePicker.textChanges(),
            buttonTimePicker.textChanges(),
            etTaskDescription.textChanges().map { it.isBlank().not() },
            Function3<CharSequence, CharSequence, Boolean, Boolean> { date, time, descriptionIsNotEmpty ->
                SimpleDateFormat(DATE_PATTERN_FOR_LOGGING, Locale.getDefault()).apply { isLenient = false }.parse("$date $time")!!.time > System.currentTimeMillis() + 60000L && descriptionIsNotEmpty
            })
            .subscribe {
                if (dialog != null) {
                    (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)?.isEnabled = it
                } else {
                    buttonOk.isEnabled = it
                }
            }

        clicks += Observable.combineLatest(rbDayOfWeek.checkedChanges(), rbXTimeUnit.checkedChanges(), BiFunction<Boolean, Boolean, Unit> { dayOfWeek, xTimeUnit ->
            rbXTimeUnit.isChecked = xTimeUnit && dayOfWeek.not()
            rbDayOfWeek.isChecked = xTimeUnit.not() && dayOfWeek
            containerDayOfWeek.setBackgroundColor(if (xTimeUnit.not() && dayOfWeek) Color.GREEN else Color.WHITE)
            containerEveryXTimeunit.setBackgroundColor(if (xTimeUnit && dayOfWeek.not()) Color.GREEN else Color.WHITE)
        }).subscribe()
    }

    @SuppressLint("SetTextI18n")
    override fun onTimeSet(hourOfDay: Int, minutes: Int) {
        val minutesWithLeadingZeroIfNecessary = if (minutes < 10) "0$minutes" else minutes.toString()
        buttonTimePicker.text = "$hourOfDay:$minutesWithLeadingZeroIfNecessary"
    }

    override fun onDateSet(year: Int, month: Int, day: Int) {
        buttonDatePicker.text = SimpleDateFormat(DATE_PATTERN_DAY_MONTH_YEAR, Locale.getDefault()).format(Date(year - 1900, month, day))
    }

    interface TimeSettingCallback {
        fun onTimeSet(description: String, repeatingClassifier: RepeatingClassifier, repeatingClassifierValue: String, time: String)
    }
}