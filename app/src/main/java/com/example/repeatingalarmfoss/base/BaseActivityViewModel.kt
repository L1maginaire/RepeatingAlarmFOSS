package com.example.repeatingalarmfoss.base

import androidx.lifecycle.LiveData
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.SingleLiveEvent
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class BaseActivityViewModel @Inject constructor(private val prefInteractor: BaseActivitySettingsInteractor): BaseViewModel() {
    private val _setNightModeValueAndRecreateEvent = SingleLiveEvent<Int>()
    val nightModeChangedEvent: LiveData<Int> get() = _setNightModeValueAndRecreateEvent

    private val _recreateEvent = SingleLiveEvent<Any>()
    val recreateEvent: LiveData<Any> get() = _recreateEvent

    private val _errorEvent = SingleLiveEvent<Int>()
    val errorEvent: LiveData<Int> get() = _errorEvent

    fun checkNightModeState(toBeCompared: Int) {
        disposable += prefInteractor.handleThemeChanges(toBeCompared).subscribe {
            when (it) {
                is NightModeChangesResult.Success -> _setNightModeValueAndRecreateEvent.value = it.code
                is NightModeChangesResult.SharedChangesCorruptionError -> _errorEvent.value = R.string.db_error
            }
        }
    }

    fun checkLocaleChanged(currentLocale: String) {
        disposable += prefInteractor.checkLocaleChanged(currentLocale).subscribe {
            when (it) {
                is LocaleChangedResult.Success -> _recreateEvent.call()
                is LocaleChangedResult.SharedPreferencesCorruptionError -> _errorEvent.value = R.string.db_error
            }
        }
    }
}
