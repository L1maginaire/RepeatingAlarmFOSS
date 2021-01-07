package com.example.repeatingalarmfoss.screens.alarm

import androidx.lifecycle.LiveData
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.base.BaseViewModel
import com.example.repeatingalarmfoss.helper.SingleLiveEvent
import com.example.repeatingalarmfoss.repositories.GetMissedAlarmCounterResult
import com.example.repeatingalarmfoss.repositories.MissedAlarmCounterPreferencesRepository
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class AlarmActivityViewModel @Inject constructor(private val missedAlarmCounterRepo: MissedAlarmCounterPreferencesRepository) : BaseViewModel() {
    private val _getMissedAlarmsCounterEvent = SingleLiveEvent<Int>()
    val getMissedAlarmsCounterEvent: LiveData<Int> get() = _getMissedAlarmsCounterEvent

    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

    fun getMissedAlarmCounter(taskId: Long) {
        disposable += missedAlarmCounterRepo.getAndUpdateMissedAlarmsCounter(taskId)
            .subscribe(Consumer {
                when (it) {
                    is GetMissedAlarmCounterResult.Success -> _getMissedAlarmsCounterEvent.value = it.counter
                    is GetMissedAlarmCounterResult.DatabaseCorruptionError -> _errorEvent.value = R.string.db_error
                }
            })
    }
}