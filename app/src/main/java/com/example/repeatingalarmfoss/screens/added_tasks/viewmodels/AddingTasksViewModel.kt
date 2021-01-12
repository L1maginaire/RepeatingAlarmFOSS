package com.example.repeatingalarmfoss.screens.added_tasks.viewmodels

import androidx.lifecycle.LiveData
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.base.BaseViewModel
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.db.Task
import com.example.repeatingalarmfoss.helper.SingleLiveEvent
import com.example.repeatingalarmfoss.screens.added_tasks.*
import com.example.repeatingalarmfoss.usecases.AddTaskResult
import com.example.repeatingalarmfoss.usecases.DeleteTaskResult
import com.example.repeatingalarmfoss.usecases.FetchTasksResult
import com.example.repeatingalarmfoss.usecases.TaskInteractor
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class AddingTasksViewModel @Inject constructor(private val taskInteractor: TaskInteractor) : BaseViewModel() {
    private val _addTaskEvent = SingleLiveEvent<TaskUi>()
    val addTaskEvent: LiveData<TaskUi> get() = _addTaskEvent

    private val _removeTaskEvent = SingleLiveEvent<Long>()
    val removeTaskEvent: LiveData<Long> get() = _removeTaskEvent

    private val _scheduleTaskEvent = SingleLiveEvent<Task>()
    val scheduleTaskEvent: LiveData<Task> get() = _scheduleTaskEvent

    private val _fetchAllTasksEvent = SingleLiveEvent<List<TaskUi>>()
    val fetchAllTasksEvent: LiveData<List<TaskUi>> get() = _fetchAllTasksEvent

    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

    fun removeTask(id: Long) {
        disposable += taskInteractor.delete(id)
            .subscribe(Consumer {
                when (it) {
                    is DeleteTaskResult.Success -> _removeTaskEvent.value = it.id
                    is DeleteTaskResult.DatabaseCorruptionError -> _errorEvent.value = R.string.db_error
                }
            })
    }

    fun addTask(description: String, repeatingClassifier: RepeatingClassifier, repeatingClassifierValue: String, time: String) {
        disposable += taskInteractor.addTask(description, repeatingClassifier, repeatingClassifierValue, time)
            .subscribe(Consumer {
                when (it) {
                    is AddTaskResult.Success -> {
                        _scheduleTaskEvent.value = it.task.first
                        _addTaskEvent.value = it.task.second
                    }
                    is AddTaskResult.Failure.DatabaseCorruption -> _errorEvent.value = R.string.db_error
                }
            })
    }

    fun fetchTasks() {
        disposable += taskInteractor.fetchTasks().subscribe(Consumer {
            when (it) {
                is FetchTasksResult.Success -> _fetchAllTasksEvent.value = it.tasks
                is FetchTasksResult.Failure.DatabaseCorruption -> _errorEvent.value = R.string.db_error
            }
        })
    }
}