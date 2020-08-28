package com.example.repeatingalarmfoss

import android.os.Bundle
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.repeatingalarmfoss.db.RepeatingClassifier
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

    private fun removeTask(id: Long) = tasksViewModel.removeTask(id)

    override fun onDestroy() = super.onDestroy().also { clicks.clear() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clicks += addTaskFab.clicks()
            .throttleFirst(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                val dialogView = (root as ViewGroup).inflate(R.layout.dialog_creating_task)
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.add_new_task))
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss().also {
                        val description = dialogView.findViewById<EditText>(R.id.etTaskDescription).text.toString()
                        val repeatingClassifier: RepeatingClassifier = RepeatingClassifier.DAY_OF_WEEK /*fixme*/
                        val repeatingClassifierValue: String = dialogView.findViewById<Spinner>(R.id.spinnerDayOfWeek).selectedItem.toString()
                        tasksViewModel.addTask(description, repeatingClassifier, repeatingClassifierValue) } }
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
}