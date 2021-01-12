package com.example.repeatingalarmfoss.di.modules

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.repeatingalarmfoss.db.TaskLocalDataSource
import com.example.repeatingalarmfoss.db.TasksDb
import com.example.repeatingalarmfoss.db.missed_alarms_counter.MissedAlarmsCountersDao
import com.example.repeatingalarmfoss.db.missed_alarms_counter.MissedAlarmsCountersDb
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

private const val DATABASE_NAME_TASKS = "database-tasks"
private const val DATABASE_NAME_MISSED_COUNTERS = "database-missed-counters"

@Module
class DatabaseModule {
    private val migration1to2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) = database.execSQL("ALTER TABLE Task ADD COLUMN repeatingClassifier TEXT DEFAULT \"DAY_OF_WEEK\" NOT NULL")
            .also { database.execSQL("ALTER TABLE Task ADD COLUMN repeatingClassifierValue TEXT NOT NULL") }
    }

    private val migration2to3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) = database.execSQL("ALTER TABLE Task ADD COLUMN time TEXT NOT NULL")
    }

    @Provides
    @Singleton
    fun provideTasksDatabase(context: Context): TasksDb = Room.databaseBuilder(context, TasksDb::class.java, DATABASE_NAME_TASKS)
        .addMigrations(migration1to2)
        .addMigrations(migration2to3)
        .build()

    @Provides
    @Singleton
    fun provideTaskRepository(db: TasksDb): TaskLocalDataSource = db.taskDao()

    @Provides
    @Singleton
    fun provideMissedAlarmsCountersDatabase(context: Context): MissedAlarmsCountersDb = Room.databaseBuilder(context, MissedAlarmsCountersDb::class.java, DATABASE_NAME_MISSED_COUNTERS)
        .allowMainThreadQueries()
        .build()

    @Provides
    @Singleton
    fun provideMissedAlarmsCountersDao(db: MissedAlarmsCountersDb): MissedAlarmsCountersDao = db.getCountersDao()
}