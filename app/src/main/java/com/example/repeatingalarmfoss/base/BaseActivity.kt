@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "WrongConstant", "RedundantSamConstructor")

package com.example.repeatingalarmfoss.base

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.helper.extensions.getLocalesLanguage
import com.example.repeatingalarmfoss.helper.extensions.provideUpdatedContextWithNewLocale
import com.example.repeatingalarmfoss.helper.extensions.toast
import io.reactivex.disposables.CompositeDisposable

open class BaseActivity : AppCompatActivity() {
    private val viewModel: BaseActivityViewModel by viewModels()
    protected val clicks = CompositeDisposable()
    override fun onDestroy() = super.onDestroy().also { clicks.clear() }

    private lateinit var currentLocale: String

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as RepeatingAlarmApp).appComponent.inject(viewModel)

        super.onCreate(savedInstanceState)
        currentLocale = resources.configuration.getLocalesLanguage()

        viewModel.errorEvent.observe(this, Observer {
            toast(getString(it))
        })
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