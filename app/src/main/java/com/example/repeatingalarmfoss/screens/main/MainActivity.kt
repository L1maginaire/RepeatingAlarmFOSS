package com.example.repeatingalarmfoss.screens.main

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.repeatingalarmfoss.ACTION_RING
import com.example.repeatingalarmfoss.AlarmReceiver
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.helper.DEFAULT_UI_SKIP_DURATION
import com.example.repeatingalarmfoss.helper.extensions.inflate
import com.example.repeatingalarmfoss.helper.extensions.set
import com.example.repeatingalarmfoss.helper.extensions.toast
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val clicks = CompositeDisposable()
    private val tasksViewModel: TasksViewModel by viewModels()
    private val tasksAdapter = TasksAdapter(::removeTask)

    override fun onDestroy() = super.onDestroy().also { clicks.clear() }

    private fun removeTask(id: Long) = tasksViewModel.removeTask(id)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clicks += addTaskFab.clicks()
            .throttleFirst(DEFAULT_UI_SKIP_DURATION, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                val dialogView = (root as ViewGroup).inflate(R.layout.dialog_creating_task)
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.add_new_task))
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        dialog.dismiss().also {
                            val description = dialogView.findViewById<EditText>(R.id.etTaskDescription).text.toString()
                            val repeatingClassifier: RepeatingClassifier = RepeatingClassifier.DAY_OF_WEEK /*fixme*/
                            val repeatingClassifierValue: String = dialogView.findViewById<Spinner>(R.id.spinnerDayOfWeek).selectedItem.toString()
                            tasksViewModel.addTask(description, repeatingClassifier, repeatingClassifierValue)
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()
                    .show()
            }
        setupViewModelSubscriptions()

        tasksList.layoutManager = LinearLayoutManager(this)
        tasksList.adapter = tasksAdapter
        tasksViewModel.fetchTasks()
    }

    private fun setupViewModelSubscriptions() {
        tasksViewModel.addTaskEvent.observe(this, Observer { task ->
            runAlarmManager(task.repeatingClassifierValue!!.toLong())
            tasksAdapter.addNewTask(task)
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

    private fun runAlarmManager(interval: Long) {
        val startTime = SystemClock.elapsedRealtime() + interval
        if (System.currentTimeMillis() < startTime) {
            val intent = Intent(this, AlarmReceiver::class.java).apply { action = ACTION_RING }
            (getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.set(SystemClock.elapsedRealtime() + interval, PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
        } else throw IllegalStateException()
    }
}