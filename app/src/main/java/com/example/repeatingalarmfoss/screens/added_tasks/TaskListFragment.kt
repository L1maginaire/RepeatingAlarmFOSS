package com.example.repeatingalarmfoss.screens.added_tasks

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.helper.DEFAULT_UI_SKIP_DURATION
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.set
import com.example.repeatingalarmfoss.helper.extensions.toast
import com.example.repeatingalarmfoss.receivers.*
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_task_list.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TaskListFragment : Fragment(), SetupAddingTaskFragment.TimeSettingCallback {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var logger: FlightRecorder
    private val addingTasksViewModel by viewModels<AddingTasksViewModel> { viewModelFactory }
    private lateinit var onTaskAddedCallback: TaskAddedCallback

    companion object {
        fun newInstance(onTaskAddedCallback: TaskAddedCallback) = TaskListFragment().apply {
            this.onTaskAddedCallback = onTaskAddedCallback
        }
    }

    private val clicks = CompositeDisposable()
    private val tasksAdapter = AddedTasksAdapter(::removeTask)
    private fun removeTask(id: Long) = addingTasksViewModel.removeTask(id)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_task_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity().application as RepeatingAlarmApp).appComponent.addTaskComponent().create().inject(this)

        if (requireActivity().root == null) {
            addTaskFab.isVisible = false
        } else {
            setupClicks()
        }
        setupViewModelSubscriptions()
        setupTaskList()
    }

    private fun setupTaskList() {
        tasksList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tasksAdapter
        }
        addingTasksViewModel.fetchTasks()
    }

    private fun setupClicks() {
        clicks += addTaskFab.clicks()
            .throttleFirst(DEFAULT_UI_SKIP_DURATION, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe { SetupAddingTaskFragment.newInstance(this).show(childFragmentManager, SetupAddingTaskFragment::class.java.simpleName) }
    }

    private fun setupViewModelSubscriptions() {
        addingTasksViewModel.addTaskEvent.observe(viewLifecycleOwner, Observer { task ->
            scheduleAlarmManager(task.description, task.repeatingClassifier, task.repeatingClassifierValue, task.time)
            tasksAdapter.addNewTask(task)
            onTaskAddedCallback.onSuccessfulScheduling()
        })
        addingTasksViewModel.removeTaskEvent.observe(viewLifecycleOwner, Observer { id ->
            cancelAlarmManagerFor(id)
            tasksAdapter.removeTask(id)
        })
        addingTasksViewModel.fetchAllTasksEvent.observe(viewLifecycleOwner, Observer {
            tasksAdapter.tasks = it.toMutableList()
        })
        addingTasksViewModel.errorEvent.observe(viewLifecycleOwner, Observer { errorMessage ->
            toast(getString(errorMessage))
        })
    }

    private fun cancelAlarmManagerFor(id: Long) {
        //TODO
    }

    private fun scheduleAlarmManager(title: String, repeatingClassifier: RepeatingClassifier, repeatingClassifierValue: String, time: String) {
        val intent = Intent(requireActivity(), AlarmReceiver::class.java).apply {
            action = ACTION_RING
            putExtra(ALARM_ARG_TITLE, title)
            putExtra(ALARM_ARG_INTERVAL, repeatingClassifierValue)
            putExtra(ALARM_ARG_CLASSIFIER, repeatingClassifier.name)
            putExtra(ALARM_ARG_TIME, time)
        }
        val nextLaunchTime: Long = if (repeatingClassifier == RepeatingClassifier.DAY_OF_WEEK) addingTasksViewModel.getNextLaunchTime(time, repeatingClassifierValue) else time.toLong()

        logger.logScheduledEvent(what = { "First launch:" }, `when` = nextLaunchTime)

        (requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager).set(nextLaunchTime, PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
    }

    override fun onTimeSet(description: String, repeatingClassifier: RepeatingClassifier, repeatingClassifierValue: String, time: String) = addingTasksViewModel.addTask(description, repeatingClassifier, repeatingClassifierValue, time)
}

interface TaskAddedCallback {
    fun onSuccessfulScheduling()
}