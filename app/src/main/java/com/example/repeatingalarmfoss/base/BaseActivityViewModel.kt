package com.example.repeatingalarmfoss.base

import androidx.lifecycle.LiveData
import com.example.repeatingalarmfoss.helper.SingleLiveEvent
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class BaseActivityViewModel @Inject constructor(private val prefInteractor: BaseActivitySettingsInteractor): BaseViewModel() {
    private val _setNightModeValueAndRecreateEvent = SingleLiveEvent<Int>()
    val nightModeChangedEvent: LiveData<Int> get() = _setNightModeValueAndRecreateEvent

    private val _recreateEvent = SingleLiveEvent<Any>()
    val recreateEvent: LiveData<Any> get() = _recreateEvent

    fun checkNightModeState(toBeCompared: Int) {
        disposable += prefInteractor.handleThemeChanges(toBeCompared).subscribe {
            if (it is NightModeChangesResult.Success) {
                _setNightModeValueAndRecreateEvent.value = it.code
            }
        }
    }

    fun checkLocaleChanged(currentLocale: String) {
        disposable += prefInteractor.checkLocaleChanged(currentLocale).subscribe {
            if (it is LocaleChangedResult.Success) {
                _recreateEvent.call()
            }
        }
    }
}
