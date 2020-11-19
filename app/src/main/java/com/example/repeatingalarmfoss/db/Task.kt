package com.example.repeatingalarmfoss.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class Task(val description: String, val repeatingClassifier: RepeatingClassifier, val repeatingClassifierValue: String, val time: String, @PrimaryKey(autoGenerate = true) var id: Long = 0L) : Parcelable
