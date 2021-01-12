package com.example.repeatingalarmfoss.db.missed_alarms_counter

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MissedAlarmsCounter(val taskId: Long, val counter: Int, @PrimaryKey(autoGenerate = true) var id: Long = 0L)