package com.example.repeatingalarmfoss.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Task::class], version = 1)
@TypeConverters(RepeatingClassifierConverter::class)
abstract class TasksDb : RoomDatabase() {
    abstract fun taskRepository(): TaskRepository
}