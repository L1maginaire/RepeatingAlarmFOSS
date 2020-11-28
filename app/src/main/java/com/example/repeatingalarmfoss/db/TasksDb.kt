package com.example.repeatingalarmfoss.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Task::class], version = 2)
@TypeConverters(RepeatingClassifierConverter::class)
abstract class TasksDb : RoomDatabase() {
    abstract fun taskRepository(): TaskLocalDataSource
}