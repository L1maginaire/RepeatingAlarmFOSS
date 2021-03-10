@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "WrongConstant", "RedundantSamConstructor")

package com.example.repeatingalarmfoss.base

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.helper.SingleLiveEvent
import com.example.repeatingalarmfoss.helper.extensions.provideUpdatedContextWithNewLocale
import com.example.repeatingalarmfoss.usecases.DarkThemeInteractor
import com.example.repeatingalarmfoss.usecases.NightModeChangesResult
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

open class BaseActivity(@LayoutRes layout: Int) : AppCompatActivity(layout) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<BaseActivityViewModel> { viewModelFactory }

    protected val subscriptions = CompositeDisposable()
    override fun onDestroy() = super.onDestroy().also { subscriptions.clear() }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as RepeatingAlarmApp).appComponent.inject(this)

        super.onCreate(savedInstanceState)
        setupViewModelSubscriptions()
        setupClicks()

        viewModel.checkNightModeState(AppCompatDelegate.getDefaultNightMode())
    }

    protected open fun setupClicks() = Unit

    @CallSuper
    protected open fun setupViewModelSubscriptions() {
        viewModel.nightModeChangedEvent.observe(this, {
            AppCompatDelegate.setDefaultNightMode(it)
            recreate()
        })
    }

    override fun attachBaseContext(base: Context) = super.attachBaseContext(base.provideUpdatedContextWithNewLocale())

    class BaseActivityViewModel @Inject constructor(private val prefInteractor: DarkThemeInteractor) : BaseViewModel() {
        private val _setNightModeValueAndRecreateEvent = SingleLiveEvent<Int>()
        val nightModeChangedEvent: LiveData<Int> get() = _setNightModeValueAndRecreateEvent

        fun checkNightModeState(toBeCompared: Int) {
            disposable += prefInteractor.handleThemeChanges(toBeCompared).subscribe {
                if (it is NightModeChangesResult.Success) {
                    _setNightModeValueAndRecreateEvent.value = it.code
                }
            }
        }
    }
}