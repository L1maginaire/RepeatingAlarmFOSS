package com.example.repeatingalarmfoss.db.missed_alarms_counter

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MissedAlarmsCounter::class], version = 1)
abstract class MissedAlarmsCountersDb : RoomDatabase() {
    abstract fun getCountersDao(): MissedAlarmsCountersDao
}
