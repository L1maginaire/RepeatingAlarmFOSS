package com.example.repeatingalarmfoss

import android.app.Application
import androidx.room.Room
import com.example.repeatingalarmfoss.db.TaskRepository
import com.example.repeatingalarmfoss.db.TasksDb

class RepeatingAlarmApp: Application() {
    lateinit var taskRepository: TaskRepository

    override fun onCreate() {
        super.onCreate()
        taskRepository = Room.databaseBuilder(applicationContext, TasksDb::class.java, "database-name").build().taskRepository()
    }
}