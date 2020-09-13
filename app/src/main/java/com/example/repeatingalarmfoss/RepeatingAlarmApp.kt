package com.example.repeatingalarmfoss

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.repeatingalarmfoss.db.TaskRepository
import com.example.repeatingalarmfoss.db.TasksDb

class RepeatingAlarmApp: Application() {
    lateinit var taskRepository: TaskRepository

    companion object { lateinit var INSTANCE: RepeatingAlarmApp }

    init { INSTANCE = this }

    private val migration1to2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) = database.execSQL("ALTER TABLE Task ADD COLUMN repeatingClassifier TEXT DEFAULT \"DAY_OF_WEEK\" NOT NULL")
            .also { database.execSQL("ALTER TABLE Task ADD COLUMN repeatingClassifierValue TEXT") }
    }

    override fun onCreate() {
        super.onCreate()
        taskRepository = Room.databaseBuilder(applicationContext, TasksDb::class.java, "database-name").addMigrations(migration1to2).build().taskRepository()
    }
}