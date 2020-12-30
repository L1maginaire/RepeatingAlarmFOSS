package com.example.repeatingalarmfoss.screens.added_tasks.viewmodels

import androidx.lifecycle.LiveData
import com.example.repeatingalarmfoss.base.BaseViewModel
import com.example.repeatingalarmfoss.helper.SingleLiveEvent
import com.example.repeatingalarmfoss.helper.rx.BaseComposers
import com.example.repeatingalarmfoss.repositories.PermissionToShowRateDialogResult
import com.example.repeatingalarmfoss.repositories.PreferencesRepository
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val preferenceRepository: PreferencesRepository, private val baseComposers: BaseComposers) : BaseViewModel() {
    private val _showRateMyAppEvent = SingleLiveEvent<Any>()
    val showRateMyAppEvent: LiveData<Any> get() = _showRateMyAppEvent

    fun checkShowRateMyApp() {
        disposable += preferenceRepository.isPermittedToShowRateDialog()
            .compose(baseComposers.commonSingleFetchTransformer())
            .subscribe(Consumer {
                if (it is PermissionToShowRateDialogResult.Success && it.toShow) {
                    _showRateMyAppEvent.call()
                }
            })
    }
}
