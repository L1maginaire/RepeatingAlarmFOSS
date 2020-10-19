package com.example.repeatingalarmfoss.screens.main

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.repeatingalarmfoss.*
import com.example.repeatingalarmfoss.db.DayOfWeek
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.helper.DEFAULT_UI_SKIP_DURATION
import com.example.repeatingalarmfoss.helper.extensions.inflate
import com.example.repeatingalarmfoss.helper.extensions.set
import com.example.repeatingalarmfoss.helper.extensions.toast
import com.example.repeatingalarmfoss.screens.AlarmActivity
import com.example.repeatingalarmfoss.screens.DatePickerFragment
import com.example.repeatingalarmfoss.screens.TimePickerFragment
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.checkedChanges
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_creating_task.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), DatePickerFragment.OnDateSetCallback, TimePickerFragment.OnTimeSetCallback {
    private val clicks = CompositeDisposable()
    private val tasksViewModel: TasksViewModel by viewModels()
    private val tasksAdapter = TasksAdapter(::removeTask)

    private lateinit var dialogsTimeSetTextView: TextView

    override fun onDestroy() = super.onDestroy().also { clicks.clear() }

    private fun removeTask(id: Long) = tasksViewModel.removeTask(id)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupClicks()
        setupViewModelSubscriptions()
        setupTaskList()
    }

    private fun setupTaskList() {
        tasksList.layoutManager = LinearLayoutManager(this)
        tasksList.adapter = tasksAdapter
        tasksViewModel.fetchTasks()
    }

    private fun setupClicks() {
        clicks += addTaskFab.clicks()
            .throttleFirst(DEFAULT_UI_SKIP_DURATION, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe { showAddTaskDialog() }
    }

    private fun setupViewModelSubscriptions() {
        tasksViewModel.addTaskEvent.observe(this, Observer { pair ->
            runAlarmManager(pair.second, pair.first.description, pair.first.repeatingClassifierValue!!) /*fixme*/
            tasksAdapter.addNewTask(pair.first)
        })
        tasksViewModel.removeTaskEvent.observe(this, Observer { id ->
            tasksAdapter.removeTask(id)
        })
        tasksViewModel.fetchAllTasksEvent.observe(this, Observer {
            tasksAdapter.tasks = it.toMutableList()
        })
        tasksViewModel.errorEvent.observe(this, Observer { errorMessage ->
            toast(getString(errorMessage))
        })
    }

    private fun runAlarmManager(firstRun: Long, title: String, repeatingClassifierValue: String) {
        val interval = repeatingClassifierValue.split("").filter { it.isEmpty().not() }.map { it.toInt() }
        val nextDay: Int = interval.firstOrNull { it >= Calendar.getInstance().get(Calendar.DAY_OF_WEEK) } ?: interval.min()!!
        val hours = SimpleDateFormat("HH:mm", Locale.UK).format(firstRun).split(":")[0].toInt()
        val minutes = SimpleDateFormat("HH:mm", Locale.UK).format(firstRun).split(":")[1].toInt()
        var nextLaunchTime = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, nextDay)
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (nextLaunchTime <= System.currentTimeMillis()) {
            nextLaunchTime = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, interval.firstOrNull { it > Calendar.getInstance().get(Calendar.DAY_OF_WEEK) }!!)
                set(Calendar.HOUR_OF_DAY, hours)
                set(Calendar.MINUTE, minutes)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            action = ACTION_RING
            putExtra(ALARM_ARG_TITLE, title)
            putExtra(ALARM_ARG_INTERVAL, repeatingClassifierValue)
            putExtra(ALARM_ARG_TIME, SimpleDateFormat("HH:mm", Locale.UK).format(firstRun))
        }
        Log.d("ABC", "abc first launch: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.UK).format(nextLaunchTime)}")

//        val startTime = SystemClock.elapsedRealtime() + interval
//        if (System.currentTimeMillis() < startTime) {
//            (getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.set(SystemClock.elapsedRealtime() + interval, PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
        (getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.set(nextLaunchTime, PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
    }

    private fun scheduleAlarmForWeekDay(weekDay: Int, hours: Int, minutes: Int) {
        Log.d("asb", "abcc $weekDay, $hours, $minutes")
        val calendar = Calendar.getInstance().apply {/*todo to ViewModel*/
            set(Calendar.DAY_OF_WEEK, weekDay)
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        (getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY * 7,
            PendingIntent.getActivity(this, 0, Intent(this, AlarmActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP) }, 0))
    }

    private fun showAddTaskDialog() {
        var weekBitSet = BitSet(7)
        val dialogView = (root as ViewGroup).inflate(R.layout.dialog_creating_task)
        clicks += dialogView.findViewById<Button>(R.id.initialDatePickerButton).clicks()
            .throttleFirst(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                DatePickerFragment(this).show(supportFragmentManager, "datepicker")
            }
        clicks += dialogView.findViewById<Button>(R.id.notificationTimeSetButton).clicks()
            .throttleFirst(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                TimePickerFragment(this).show(supportFragmentManager, "timepicker")
            }
        dialogsTimeSetTextView = dialogView.findViewById(R.id.timeSetValueTextView)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_new_task))
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
//                if (tasksViewModel.initialTimeForNewAddingTask <= System.currentTimeMillis()) {
//                    toast(getString(R.string.toast_set_proper_time))
//                } else {
                    dialog.dismiss().also {
                        val description = dialogView.findViewById<EditText>(R.id.etTaskDescription).text.toString()
                        val repeatingClassifier: RepeatingClassifier = RepeatingClassifier.DAY_OF_WEEK /*fixme*/
                        val hoursAndMinutes = dialogsTimeSetTextView.text.split(":")
                        var repeaingClassifierValue = ""
                            /*todo RX-way*/
                            if(dialogView.findViewById<ToggleButton>(R.id.toggleMon).isChecked) repeaingClassifierValue += "2"
                            if(dialogView.findViewById<ToggleButton>(R.id.toggleTue).isChecked) repeaingClassifierValue += "3"
                            if(dialogView.findViewById<ToggleButton>(R.id.toggleWed).isChecked) repeaingClassifierValue += "4"
                            if(dialogView.findViewById<ToggleButton>(R.id.toggleThu).isChecked) repeaingClassifierValue += "5"
                            if(dialogView.findViewById<ToggleButton>(R.id.toggleFri).isChecked) repeaingClassifierValue += "6"
                            if(dialogView.findViewById<ToggleButton>(R.id.toggleSat).isChecked) repeaingClassifierValue += "7"
                            if(dialogView.findViewById<ToggleButton>(R.id.toggleSun).isChecked) repeaingClassifierValue += "1"
                        Log.d("y", "yay $repeaingClassifierValue")
                        tasksViewModel.addTask(description, repeatingClassifier, repeaingClassifierValue)
//                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .show()
    }

    override fun onDateSet(year: Int, month: Int, day: Int) = tasksViewModel.persistAddingTasksInitialDate(year, month, day)
    @SuppressLint("SetTextI18n")
    override fun onTimeSet(hourOfDay: Int, minutes: Int) = tasksViewModel.persistAddingTasksTime(hourOfDay, minutes).also { dialogsTimeSetTextView.text = "$hourOfDay:$minutes" }
}