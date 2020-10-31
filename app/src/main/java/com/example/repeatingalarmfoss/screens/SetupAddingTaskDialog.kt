package com.example.repeatingalarmfoss.screens

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.helper.DEFAULT_UI_SKIP_DURATION
import com.example.repeatingalarmfoss.helper.FlightRecorder
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

class SetupAddingTaskDialog(private val timeSettingCallback: TimeSettingCallback) : DialogFragment(), TimePickerFragment.OnTimeSetCallback, DatePickerFragment.OnDateSetCallback {
    private val clicks = CompositeDisposable()
    private val logger = FlightRecorder.getInstance()
    private val chosenWeekDays = BitSet(7)
    private lateinit var customView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = customView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        customView = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_creating_task, null)
        return AlertDialog.Builder(requireActivity())
            .setTitle(getString(R.string.add_new_task))
            .setView(customView)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss().also {
                    val description = etTaskDescription.text.toString()
                    val time = buttonTimePicker.text.toString()
                    val repeatingClassifier: RepeatingClassifier
                    if (rbDayOfWeek.isChecked) {
                        repeatingClassifier = RepeatingClassifier.DAY_OF_WEEK
                        timeSettingCallback.onTimeSet(description, repeatingClassifier, chosenWeekDays.toString(), time)
                        logger.d(true) { "chosen week days in dialog: $chosenWeekDays" }
                    } else if(rbXTimeUnit.isChecked){
                        val currentSpinnerValue = spinnerTimeUnits.selectedItem.toString()
                        repeatingClassifier = RepeatingClassifier.EVERY_X_TIME_UNIT
                        val repeatingClassifierValue = etEveryXValue.text

                        logger.d(true) { "chosen date in dialog: ${SimpleDateFormat("dd MMM yyyy HH:mm").apply { isLenient = false }.parse(buttonDatePicker.text.toString() + " " + buttonTimePicker.text.toString())}" }

                        timeSettingCallback.onTimeSet(description, repeatingClassifier, repeatingClassifierValue.toString()+currentSpinnerValue,
                            SimpleDateFormat("dd MMM yyyy HH:mm").apply { isLenient = false }.parse(buttonDatePicker.text.toString() + " " + buttonTimePicker.text.toString()).time.toString())
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create().apply {
                setOnShowListener {
                    buttonTimePicker.text = SimpleDateFormat("HH:mm").format(Date())
                    buttonDatePicker.text = SimpleDateFormat("dd MMM yyyy").format(Date())
                    setupClicks(chosenWeekDays)
                }
            }
    }

    private fun setupClicks(chosenWeekDays: BitSet) {
        clicks += buttonTimePicker.clicks()
            .throttleFirst(DEFAULT_UI_SKIP_DURATION, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe { TimePickerFragment(this).show(requireActivity().supportFragmentManager, TimePickerFragment::class.java.simpleName) }

        clicks += buttonDatePicker.clicks()
            .throttleFirst(DEFAULT_UI_SKIP_DURATION, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe { DatePickerFragment(this).show(requireActivity().supportFragmentManager, TimePickerFragment::class.java.simpleName) }

        clicks += Observable.combineLatest(toggleMon.checkedChanges(), toggleTue.checkedChanges(), toggleWed.checkedChanges(), toggleThu.checkedChanges(), toggleFri.checkedChanges(), toggleSat.checkedChanges(), toggleSun.checkedChanges(),
            Function7<Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Unit> { mon, tue, wed, thu, fri, sat, sun ->
                chosenWeekDays.apply {
                    if (mon) set(0) else clear(0)
                    if (tue) set(1) else clear(1)
                    if (wed) set(2) else clear(2)
                    if (thu) set(3) else clear(3)
                    if (fri) set(4) else clear(4)
                    if (sat) set(5) else clear(5)
                    if (sun) set(6) else clear(6)
                }
            }).subscribe()

        clicks += Observable.combineLatest(buttonDatePicker.textChanges(),
            buttonTimePicker.textChanges(),
            etTaskDescription.textChanges().map { it.isBlank().not() },
            Function3<CharSequence, CharSequence, Boolean, Boolean> { date, time, descriptionIsNotEmpty ->
                SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).apply { isLenient = false }.parse("$date $time")!!.time > System.currentTimeMillis() + 60000L && descriptionIsNotEmpty
            })
            .subscribe { (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = it }

        clicks += Observable.combineLatest(rbDayOfWeek.checkedChanges(), rbXTimeUnit.checkedChanges(), BiFunction<Boolean, Boolean, Unit> { dayOfWeek, xTimeUnit ->
            rbXTimeUnit.isChecked = xTimeUnit && dayOfWeek.not()
            rbDayOfWeek.isChecked = xTimeUnit.not() && dayOfWeek
            containerDayOfWeek.setBackgroundColor(if(xTimeUnit.not() && dayOfWeek) Color.GREEN else Color.WHITE)
            containerEveryXTimeunit.setBackgroundColor(if(xTimeUnit && dayOfWeek.not()) Color.GREEN else Color.WHITE)
        }).subscribe()
    }

    @SuppressLint("SetTextI18n")
    override fun onTimeSet(hourOfDay: Int, minutes: Int) {
        buttonTimePicker.text = "$hourOfDay:$minutes"
    }

    override fun onDateSet(year: Int, month: Int, day: Int) {
        buttonDatePicker.text = SimpleDateFormat("dd MMM yyyy").format(Date(year, month, day)) /*fixme: here's the problem with year 3920?*/
    }

    interface TimeSettingCallback {
        fun onTimeSet(description: String, repeatingClassifier: RepeatingClassifier, repeatingClassifierValue: String, time: String)
    }
}