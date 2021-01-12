package com.example.repeatingalarmfoss.repositories

import com.example.repeatingalarmfoss.db.missed_alarms_counter.MissedAlarmsCounter
import com.example.repeatingalarmfoss.db.missed_alarms_counter.MissedAlarmsCountersDao
import com.example.repeatingalarmfoss.helper.extensions.createEntityIfAbsent
import com.example.repeatingalarmfoss.helper.rx.BaseComposers
import io.reactivex.Single
import javax.inject.Inject

class MissedAlarmCounterPreferencesRepository @Inject constructor(private val missedAlarmsCountersDao: MissedAlarmsCountersDao, private val composers: BaseComposers) {
    fun getAndUpdateMissedAlarmsCounter(taskId: Long): Single<GetMissedAlarmCounterResult> = getMissedAlarmsCounter(taskId)
        .flatMap { missedAlarmsEntity ->
            missedAlarmsCountersDao.insert(missedAlarmsEntity.copy(counter = missedAlarmsEntity.counter + 1))
                .flatMap<GetMissedAlarmCounterResult> { Single.just(GetMissedAlarmCounterResult.Success(missedAlarmsEntity.counter)) }
        }
        .onErrorReturn { GetMissedAlarmCounterResult.DatabaseCorruptionError }
        .compose(composers.applySingleSchedulers())

    private fun getMissedAlarmsCounter(taskId: Long): Single<MissedAlarmsCounter> = missedAlarmsCountersDao.findByTaskId(taskId)
        .onErrorResumeNext(createEntityIfAbsent(
            missedAlarmsCountersDao.insert(MissedAlarmsCounter(taskId, 1))
                .flatMap { missedAlarmsCountersDao.findByTaskId(taskId) }
        ))
}

sealed class GetMissedAlarmCounterResult {
    data class Success(val counter: Int) : GetMissedAlarmCounterResult()
    object DatabaseCorruptionError : GetMissedAlarmCounterResult()
}