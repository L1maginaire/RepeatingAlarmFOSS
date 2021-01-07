package com.example.repeatingalarmfoss.repositories

import androidx.room.EmptyResultSetException
import com.example.repeatingalarmfoss.db.missed_alarms_counter.MissedAlarmsCounter
import com.example.repeatingalarmfoss.db.missed_alarms_counter.MissedAlarmsCountersDao
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.functions.Function
import javax.inject.Inject

class MissedAlarmCounterPreferencesRepository @Inject constructor(private val missedAlarmsCountersDao: MissedAlarmsCountersDao) {
    fun getMissedAlarmsCounter(taskId: Long): Single<GetMissedAlarmCounterResult> = missedAlarmsCountersDao.findByTaskId(taskId)
        .onErrorResumeNext(createEntityIfAbsent(missedAlarmsCountersDao, taskId))
        .map { it.counter }
        .map<GetMissedAlarmCounterResult> { GetMissedAlarmCounterResult.Success(it) }
        .onErrorReturn { GetMissedAlarmCounterResult.Failure }
        .doOnError { it.printStackTrace() }

    fun updateMissedAlarmsCounter(taskId: Long): Single<UpdateMissedAlarmCounterResult> = missedAlarmsCountersDao.findByTaskId(taskId)
        .onErrorResumeNext(createEntityIfAbsent(missedAlarmsCountersDao, taskId))
        .flatMap { missedAlarmsCountersDao.insert(it.copy(counter = it.counter + 1)) }
        .map<UpdateMissedAlarmCounterResult> { UpdateMissedAlarmCounterResult.Success }
        .onErrorReturn { UpdateMissedAlarmCounterResult.Failure }
        .doOnError { it.printStackTrace() }
}

fun createEntityIfAbsent(missedAlarmsCountersDao: MissedAlarmsCountersDao, taskId: Long): Function<Throwable, SingleSource<MissedAlarmsCounter>> = Function {
    if (it is EmptyResultSetException) {
        missedAlarmsCountersDao.insert(MissedAlarmsCounter(taskId, 1)).flatMap { missedAlarmsCountersDao.findByTaskId(taskId) }
    } else {
        Single.error(UnknownError())
    }
}

sealed class GetMissedAlarmCounterResult {
    data class Success(val counter: Int) : GetMissedAlarmCounterResult()
    object Failure : GetMissedAlarmCounterResult()
}

sealed class UpdateMissedAlarmCounterResult {
    object Success : UpdateMissedAlarmCounterResult()
    object Failure : UpdateMissedAlarmCounterResult()
}