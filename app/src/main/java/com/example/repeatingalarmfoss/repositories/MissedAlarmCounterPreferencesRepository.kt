package com.example.repeatingalarmfoss.repositories

import com.example.repeatingalarmfoss.db.missed_alarms_counter.MissedAlarmsCountersDao
import com.example.repeatingalarmfoss.helper.extensions.createEntityIfAbsent
import com.example.repeatingalarmfoss.helper.rx.BaseComposers
import io.reactivex.Single
import javax.inject.Inject

class MissedAlarmCounterPreferencesRepository @Inject constructor(private val missedAlarmsCountersDao: MissedAlarmsCountersDao, private val composers: BaseComposers) {
    fun getMissedAlarmsCounter(taskId: Long): Single<GetMissedAlarmCounterResult> = missedAlarmsCountersDao.findByTaskId(taskId)
        .onErrorResumeNext(createEntityIfAbsent(missedAlarmsCountersDao, taskId))
        .map { it.counter }
        .map<GetMissedAlarmCounterResult> { GetMissedAlarmCounterResult.Success(it) }
        .onErrorReturn { GetMissedAlarmCounterResult.Failure }
        .compose(composers.commonSingleFetchTransformer())

    fun updateMissedAlarmsCounter(taskId: Long): Single<UpdateMissedAlarmCounterResult> = missedAlarmsCountersDao.findByTaskId(taskId)
        .onErrorResumeNext(createEntityIfAbsent(missedAlarmsCountersDao, taskId))
        .flatMap { missedAlarmsCountersDao.insert(it.copy(counter = it.counter + 1)) }
        .map<UpdateMissedAlarmCounterResult> { UpdateMissedAlarmCounterResult.Success }
        .onErrorReturn { UpdateMissedAlarmCounterResult.Failure }
        .compose(composers.commonSingleFetchTransformer())
}

sealed class GetMissedAlarmCounterResult {
    data class Success(val counter: Int) : GetMissedAlarmCounterResult()
    object Failure : GetMissedAlarmCounterResult()
}

sealed class UpdateMissedAlarmCounterResult {
    object Success : UpdateMissedAlarmCounterResult()
    object Failure : UpdateMissedAlarmCounterResult()
}