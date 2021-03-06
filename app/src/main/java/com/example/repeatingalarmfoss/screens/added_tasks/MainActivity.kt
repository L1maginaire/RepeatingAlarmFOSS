@file:Suppress("UNSAFE_CALL_ON_PARTIALLY_DEFINED_RESOURCE", "RedundantSamConstructor")

package com.example.repeatingalarmfoss.screens.added_tasks

import android.content.Intent
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.base.BaseActivity
import com.example.repeatingalarmfoss.di.modules.Authenticator
import com.example.repeatingalarmfoss.di.modules.BiometricModule
import com.example.repeatingalarmfoss.di.scopes.BiometricScope
import com.example.repeatingalarmfoss.helper.extensions.throttleFirst
import com.example.repeatingalarmfoss.screens.added_tasks.viewmodels.MainActivityViewModel
import com.example.repeatingalarmfoss.screens.logs.LogsActivity
import com.example.repeatingalarmfoss.screens.settings.SettingsFragment
import com.jakewharton.rxbinding3.view.clicks
import com.squareup.seismic.ShakeDetector
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

/*TODO subcomponent with Fragment, Adapter, etc. dependencies*/
@BiometricScope
class MainActivity : BaseActivity(R.layout.activity_main), TaskAddedCallback, ShakeDetector.Listener {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var sensorManager: SensorManager
    @Inject lateinit var authenticator: Authenticator
    @Inject lateinit var sharedPreferences: SharedPreferences

    private val viewModel by viewModels<MainActivityViewModel> { viewModelFactory }

    private lateinit var taskListFragment: TaskListFragment
    private lateinit var setupAddingTaskFragment: SetupAddingTaskFragment
    private var isTablet = false
    private val shakeDetector: ShakeDetector by lazy { ShakeDetector(this) }
    private var bottomTabIndex = 0

    override fun onResume() = super.onResume().also { shakeDetector.start(sensorManager) }
    override fun onPause() = super.onPause().also { shakeDetector.stop() }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (isTablet.not()) {
            bottomBar.selectTabAt(bottomTabIndex)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as RepeatingAlarmApp)
            .appComponent
            .biometricComponent(BiometricModule(this, onSuccessfulAuth = { populateUi() }))
            .inject(this)

        isTablet = resources.getBoolean(R.bool.isTablet)
        super.onCreate(savedInstanceState)

        if (sharedPreferences.getBoolean(getString(R.string.pref_enable_biometric_protection), false)) {
            ivLock.isVisible = true
            controlsUi.isVisible = false

            authenticator.authenticate()

            subscriptions += ivLock.clicks()
                .throttleFirst()
                .subscribe { authenticator.authenticate() }
        } else {
            populateUi()
        }
    }

    private fun populateUi() {
        ivLock.isVisible = false
        controlsUi.isVisible = true

        if (isTablet) {
            val settingsFragment = SettingsFragment()
            taskListFragment = TaskListFragment.newInstance()
            setupAddingTaskFragment = SetupAddingTaskFragment.newInstance()

            supportFragmentManager.commit {
                replace(R.id.detailFragmentContainer, taskListFragment)
                replace(R.id.fragmentContainer, setupAddingTaskFragment)
            }
            bottomBar.isVisible = true
            bottomBar.onTabSelected = {
                supportFragmentManager.commit {
                    replace(
                        R.id.detailFragmentContainer,
                        when (it.id) {
                            R.id.tab_alarm -> taskListFragment
                            R.id.tab_settings -> settingsFragment
                            else -> throw IllegalStateException()
                        }
                    )
                }
            }
        } else {
            pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
                override fun onPageScrollStateChanged(state: Int) = Unit
                override fun onPageSelected(position: Int) {
                    bottomTabIndex = position
                }
            })
            pager.adapter = MainScreenViewPagerAdapter(supportFragmentManager)
            bottomBar.setupWithViewPager(pager)
        }

        viewModel.showRateMyAppEvent.observe(this, Observer { RateMyAppDialog().show(supportFragmentManager, RateMyAppDialog::class.java.simpleName) })
        viewModel.checkShowRateMyApp()
    }

    override fun onSuccessfulScheduling() = if (isTablet) setupAddingTaskFragment.setFieldsDefault() else Unit
    override fun hearShake() = startActivity(Intent(this, LogsActivity::class.java))
}