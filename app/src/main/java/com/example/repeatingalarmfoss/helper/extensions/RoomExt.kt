package com.example.repeatingalarmfoss.helper.extensions

import androidx.room.EmptyResultSetException
import com.example.repeatingalarmfoss.db.missed_alarms_counter.MissedAlarmsCounter
import com.example.repeatingalarmfoss.db.missed_alarms_counter.MissedAlarmsCountersDao
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.functions.Function

fun createEntityIfAbsent(missedAlarmsCountersDao: MissedAlarmsCountersDao, taskId: Long): Function<Throwable, SingleSource<MissedAlarmsCounter>> = Function {
    if (it is EmptyResultSetException) {
        missedAlarmsCountersDao.insert(MissedAlarmsCounter(taskId, 1)).flatMap { missedAlarmsCountersDao.findByTaskId(taskId) }
    } else {
        Single.error(UnknownError())
    }
}
