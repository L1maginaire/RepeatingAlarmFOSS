@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "WrongConstant", "RedundantSamConstructor")

package com.example.repeatingalarmfoss.base

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.helper.extensions.getLocalesLanguage
import com.example.repeatingalarmfoss.helper.extensions.provideUpdatedContextWithNewLocale
import com.example.repeatingalarmfoss.helper.extensions.toast
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

open class BaseActivity : AppCompatActivity() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<BaseActivityViewModel> { viewModelFactory }

    protected val subscriptions = CompositeDisposable()
    override fun onDestroy() = super.onDestroy().also { subscriptions.clear() }

    private lateinit var currentLocale: String

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as RepeatingAlarmApp).appComponent.inject(this)

        super.onCreate(savedInstanceState)
        currentLocale = resources.configuration.getLocalesLanguage()

        viewModel.recreateEvent.observe(this, Observer {
            recreate()
        })
        viewModel.nightModeChangedEvent.observe(this, Observer {
            AppCompatDelegate.setDefaultNightMode(it)
            recreate()
        })
        viewModel.checkNightModeState(AppCompatDelegate.getDefaultNightMode())
    }

    override fun onRestart() = super.onRestart().also { viewModel.checkLocaleChanged(currentLocale) }
    override fun attachBaseContext(base: Context) = super.attachBaseContext(base.provideUpdatedContextWithNewLocale())
}