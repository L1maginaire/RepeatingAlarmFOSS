package com.example.repeatingalarmfoss.screens.added_tasks

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.repeatingalarmfoss.*
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.helper.DEFAULT_UI_SKIP_DURATION
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.set
import com.example.repeatingalarmfoss.helper.extensions.toast
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TaskListActivity : AppCompatActivity(), SetupAddingTaskDialog.TimeSettingCallback {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var logger: FlightRecorder
    private val addingTasksViewModel by viewModels<AddingTasksViewModel> { viewModelFactory }

    private val clicks = CompositeDisposable()
    private val tasksAdapter = AddedTasksAdapter(::removeTask)

    override fun onDestroy() = super.onDestroy().also { clicks.clear() }

    private fun removeTask(id: Long) = addingTasksViewModel.removeTask(id)

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as RepeatingAlarmApp).appComponent.addTaskComponent().create().inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupClicks()
        setupViewModelSubscriptions()
        setupTaskList()
    }

    private fun setupTaskList() {
        tasksList.apply {
            layoutManager = LinearLayoutManager(this@TaskListActivity)
            adapter = tasksAdapter
        }
        addingTasksViewModel.fetchTasks()
    }

    private fun setupClicks() {
        clicks += addTaskFab.clicks()
            .throttleFirst(DEFAULT_UI_SKIP_DURATION, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe { SetupAddingTaskDialog(this).show(supportFragmentManager, SetupAddingTaskDialog::class.java.simpleName) }
    }

    private fun setupViewModelSubscriptions() {
        addingTasksViewModel.addTaskEvent.observe(this, Observer { task ->
            scheduleAlarmManager(task.description, task.repeatingClassifier, task.repeatingClassifierValue, task.time)
            tasksAdapter.addNewTask(task)
        })
        addingTasksViewModel.removeTaskEvent.observe(this, Observer { id ->
            cancelAlarmManagerFor(id)
            tasksAdapter.removeTask(id)
        })
        addingTasksViewModel.fetchAllTasksEvent.observe(this, Observer {
            tasksAdapter.tasks = it.toMutableList()
        })
        addingTasksViewModel.errorEvent.observe(this, Observer { errorMessage ->
            toast(getString(errorMessage))
        })
    }

    private fun cancelAlarmManagerFor(id: Long) {
        //TODO
    }

    private fun scheduleAlarmManager(title: String, repeatingClassifier: RepeatingClassifier, repeatingClassifierValue: String, time: String) {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            action = ACTION_RING
            putExtra(ALARM_ARG_TITLE, title)
            putExtra(ALARM_ARG_INTERVAL, repeatingClassifierValue)
            putExtra(ALARM_ARG_CLASSIFIER, repeatingClassifier.name)
            putExtra(ALARM_ARG_TIME, time)
        }
        val nextLaunchTime: Long = if(repeatingClassifier == RepeatingClassifier.DAY_OF_WEEK) addingTasksViewModel.getNextLaunchTime(time, repeatingClassifierValue) else time.toLong()

        logger.d(true) { "First launch: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.UK).format(nextLaunchTime)}" }

        (getSystemService(Context.ALARM_SERVICE) as AlarmManager).set(nextLaunchTime, PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
    }

    override fun onTimeSet(description: String, repeatingClassifier: RepeatingClassifier, repeatingClassifierValue: String, time: String) = addingTasksViewModel.addTask(description, repeatingClassifier, repeatingClassifierValue, time)
}