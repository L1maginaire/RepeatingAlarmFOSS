package com.example.repeatingalarmfoss.screens.added_tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.db.Task
import com.example.repeatingalarmfoss.db.TaskLocalDataSource
import com.example.repeatingalarmfoss.helper.SingleLiveEvent
import com.example.repeatingalarmfoss.usecases.NextLaunchTimeCalculationUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AddingTasksViewModel @Inject constructor(private val taskLocalDataSource: TaskLocalDataSource) : ViewModel() {
    @Inject
    lateinit var nextLaunchTimeCalculationUseCase: NextLaunchTimeCalculationUseCase

    private val disposable = CompositeDisposable()
    override fun onCleared() = disposable.clear()

    private val _addTaskEvent = SingleLiveEvent<Task>()
    val addTaskEvent: LiveData<Task> get() = _addTaskEvent

    private val _removeTaskEvent = SingleLiveEvent<Long>()
    val removeTaskEvent: LiveData<Long> get() = _removeTaskEvent

    private val _fetchAllTasksEvent = SingleLiveEvent<List<Task>>()
    val fetchAllTasksEvent: LiveData<List<Task>> get() = _fetchAllTasksEvent

    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

    fun removeTask(id: Long) {
        disposable += taskLocalDataSource.delete(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { it.printStackTrace() }
            .subscribe({
                _removeTaskEvent.value = id
            }, {
                _errorEvent.value = R.string.db_error
            })
    }

    fun addTask(description: String, repeatingClassifier: RepeatingClassifier, repeatingClassifierValue: String, time: String) {
        val task = Task(description, repeatingClassifier, repeatingClassifierValue, if (repeatingClassifier == RepeatingClassifier.DAY_OF_WEEK) getNextLaunchTime(time, repeatingClassifierValue).toString() else time)
        disposable += taskLocalDataSource.insert(task)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { it.printStackTrace() }
            .subscribe({
                _addTaskEvent.value = task.apply { id = it }
            }, {
                _errorEvent.value = R.string.db_error
            })
    }

    fun fetchTasks() {
        disposable += taskLocalDataSource.getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { it.printStackTrace() }
            .subscribe({
                _fetchAllTasksEvent.value = it
            }, {
                _errorEvent.value = R.string.db_error
            })
    }

    private fun getNextLaunchTime(time: String, chosenWeekDaysBinaryString: String): Long = nextLaunchTimeCalculationUseCase.getNextLaunchTime(time, chosenWeekDaysBinaryString)
}