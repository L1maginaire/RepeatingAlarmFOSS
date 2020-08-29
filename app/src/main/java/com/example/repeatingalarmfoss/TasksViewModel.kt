package com.example.repeatingalarmfoss

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.db.Task
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers

class TasksViewModel(app: Application) : AndroidViewModel(app) {
    private val taskRepository = (app as RepeatingAlarmApp).taskRepository
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

    fun removeTask(id: Long){
        disposable += taskRepository.delete(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError{ it.printStackTrace() }
            .subscribe(/*todo states*/{
                _removeTaskEvent.value = id
            }, {
                _errorEvent.value = R.string.db_error
            })
    }

    fun addTask(description: String, repeatingClassifier: RepeatingClassifier, repeatingClassifierValue: String?) {
        val task = Task(description, repeatingClassifier, repeatingClassifierValue)
        disposable += taskRepository.insert(task)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError{ it.printStackTrace() }
            .subscribe(/*todo states*/{
                _addTaskEvent.value = task.apply { id = it }
            }, {
                _errorEvent.value = R.string.db_error
            })
    }

    fun fetchTasks() {
        disposable += taskRepository.getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError{ it.printStackTrace() }
            .subscribe(/*todo states*/{
                _fetchAllTasksEvent.value = it
            }, {
                _errorEvent.value = R.string.db_error
            })
    }
}