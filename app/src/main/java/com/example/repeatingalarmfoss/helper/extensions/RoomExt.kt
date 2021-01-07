package com.example.repeatingalarmfoss.helper.extensions

import androidx.room.EmptyResultSetException
import com.example.repeatingalarmfoss.db.missed_alarms_counter.MissedAlarmsCounter
import com.example.repeatingalarmfoss.db.missed_alarms_counter.MissedAlarmsCountersDao
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.functions.Function

fun <T> createEntityIfAbsent(createAction: Single<T>): Function<Throwable, SingleSource<T>> = Function {
    if (it is EmptyResultSetException) {
        createAction
    } else {
        Single.error(UnknownError())
    }
}
