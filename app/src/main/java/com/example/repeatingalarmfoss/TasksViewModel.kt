package com.example.repeatingalarmfoss

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class TasksViewModel(context: Application) : AndroidViewModel(context) {
    private val _addTaskEvent = SingleLiveEvent<String>()
    val addTaskEvent: LiveData<String> get() = _addTaskEvent

    private val _removeTaskEvent = SingleLiveEvent<Long>()
    val removeTaskEvent: LiveData<Long> get() = _removeTaskEvent

    fun addTask(id: Long){

    }

    fun fetchTasks() {
        
    }
}