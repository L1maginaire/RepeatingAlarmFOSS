package com.example.repeatingalarmfoss.screens.added_tasks

import androidx.lifecycle.LiveData
import com.example.repeatingalarmfoss.base.BaseViewModel
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.SingleLiveEvent
import com.example.repeatingalarmfoss.repositories.PermissionToShowRateDialogResult
import com.example.repeatingalarmfoss.repositories.PreferencesRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val preferenceRepository: PreferencesRepository): BaseViewModel() {
    private val _showRateMyAppEvent = SingleLiveEvent<Any>()
    val showRateMyAppEvent: LiveData<Any> get() = _showRateMyAppEvent

    fun checkShowRateMyApp() {
        disposable += preferenceRepository.isPermittedToShowRateDialog()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                if (it is PermissionToShowRateDialogResult.Success) {
                    _showRateMyAppEvent.call()
                }
            })
    }
}
