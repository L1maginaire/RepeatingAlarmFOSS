package com.example.repeatingalarmfoss.screens.added_tasks

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.helper.FixedSizeBitSet
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.*
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.functions.Function7
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.dialog_creating_task.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

const val AMOUNT_DAYS_IN_WEEK = 7

class SetupAddingTaskFragment : DialogFragment(), TimePickerFragment.OnTimeSetCallback, DatePickerFragment.OnDateSetCallback {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val addingTasksViewModel by activityViewModels<AddingTasksViewModel> { viewModelFactory }

    @Inject
    lateinit var logger: FlightRecorder
    private val clicks = CompositeDisposable()
    override fun onDestroyView() = super.onDestroyView().also { clicks.clear() }
    private val chosenWeekDays = FixedSizeBitSet(AMOUNT_DAYS_IN_WEEK)
    private lateinit var customView: View

    companion object {
        fun newInstance() = SetupAddingTaskFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = customView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        buttonOk.isVisible = dialog == null
        if (dialog == null) {
            buttonTimePicker.text = now()
            buttonDatePicker.text = today()
            setupClicks()
            clicks += buttonOk.clicks()
                .throttleFirst()
                .subscribe {
                    buttonOk.isEnabled = false
                    onOkButtonClicked()
                }
        }
    }

    fun setFieldsDefault() {
        buttonTimePicker.text = now()
        buttonDatePicker.text = today()
        with(false) {
            toggleMon.isChecked = this
            toggleTue.isChecked = this
            toggleWed.isChecked = this
            toggleThu.isChecked = this
            toggleFri.isChecked = this
            toggleSat.isChecked = this
            toggleSun.isChecked = this
        }
        etTaskDescription.setText("")
        etTimeUnitValue.setText("1")
    }

    override fun onAttach(context: Context) {
        (requireActivity().application as RepeatingAlarmApp).appComponent.inject(this)
        customView = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_creating_task, null as ViewGroup?)
        super.onAttach(context)
    }

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
                buttonTimePicker.text = now()
                buttonDatePicker.text = today()
                setupClicks()
            }
        }

    private fun onOkButtonClicked() {
        val description = etTaskDescription.text.toString()
        val time = buttonTimePicker.text.toString()
        when {
            rbDayOfWeek.isChecked -> {
                addingTasksViewModel.addTask(description, RepeatingClassifier.DAY_OF_WEEK, chosenWeekDays.toString(), time)
                logger.d { "chosen week days in dialog: $chosenWeekDays" }
            }
            rbXTimeUnit.isChecked -> {
                val chosenInitialDateAndTime: Date? = SimpleDateFormat(DATE_PATTERN_FOR_LOGGING2, Locale.getDefault()).apply { isLenient = false }.parse(buttonDatePicker.text.toString() + " " + buttonTimePicker.text.toString())
                val currentSpinnerValue = spinnerTimeUnits.selectedItem.toString()
                val repeatingClassifierValue = etTimeUnitValue.text
                logger.d { "chosen date in dialog: $chosenInitialDateAndTime" }
                addingTasksViewModel.addTask(description, RepeatingClassifier.EVERY_X_TIME_UNIT, repeatingClassifierValue.toString() + currentSpinnerValue, chosenInitialDateAndTime?.time.toString())
            }
        }
    }

    private fun setupClicks() {
        clicks += buttonTimePicker.clicks()
            .throttleFirst()
            .subscribe { TimePickerFragment(this).show(requireActivity().supportFragmentManager) }

        clicks += buttonDatePicker.clicks()
            .throttleFirst()
            .subscribe { DatePickerFragment(this).show(requireActivity().supportFragmentManager) }

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
                SimpleDateFormat(DATE_PATTERN_FOR_LOGGING2, Locale.getDefault()).apply { isLenient = false }.parse("$date $time")!!.time > System.currentTimeMillis() + 60000L && descriptionIsNotEmpty
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
            containerDayOfWeek.setBackgroundColor(if (xTimeUnit.not() && dayOfWeek) ContextCompat.getColor(requireContext(), R.color.colorPrimary) else ContextCompat.getColor(requireContext(), android.R.color.transparent))
            containerEveryXTimeunit.setBackgroundColor(if (xTimeUnit && dayOfWeek.not()) ContextCompat.getColor(requireContext(), R.color.colorPrimary) else ContextCompat.getColor(requireContext(), android.R.color.transparent))
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
}