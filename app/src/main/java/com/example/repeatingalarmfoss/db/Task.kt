package com.example.repeatingalarmfoss.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity data class Task(val description: String) {
    @PrimaryKey(autoGenerate = true) var id: Long = 0L
}