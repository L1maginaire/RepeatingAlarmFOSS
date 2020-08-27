package com.example.repeatingalarmfoss

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
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

    private val createTaskDialog: AlertDialog by lazy {
        val etView = (root as ViewGroup).inflate(R.layout.et_input)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_new_task))
            .setView(etView)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                etView.findViewById<TextInputEditText>(R.id.input).setText("")/*fixme*/
                dialog.dismiss().also { tasksViewModel.addTask(etView.findViewById<TextInputEditText>(R.id.input).text.toString()) }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    private fun removeTask(id: Long) = tasksViewModel.removeTask(id)

    override fun onDestroy() = super.onDestroy().also { clicks.clear() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clicks += addTaskFab.clicks()
            .throttleFirst(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe { createTaskDialog.show() }
        setupViewModelSubscriptions()

        tasksList.layoutManager = LinearLayoutManager(this)
        tasksList.adapter = tasksAdapter
        tasksViewModel.fetchTasks()
    }

    private fun setupViewModelSubscriptions() {
        tasksViewModel.addTaskEvent.observe(this, Observer { task ->
            tasksAdapter.addNewTask(task)
        })
        tasksViewModel.removeTaskEvent.observe(this, Observer { id ->
            tasksAdapter.removeTask(id)
        })
        tasksViewModel.fetchAllTasksEvent.observe(this, Observer {
            tasksAdapter.tasks = it.toMutableList()
        })
        tasksViewModel.errorEvent.observe(this, Observer {
            toast(getString(it))
        })
    }
}