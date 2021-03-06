package com.example.repeatingalarmfoss.base

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

open class BaseViewModel: ViewModel() {
    protected val disposable = CompositeDisposable()
    override fun onCleared() = disposable.clear()
}
