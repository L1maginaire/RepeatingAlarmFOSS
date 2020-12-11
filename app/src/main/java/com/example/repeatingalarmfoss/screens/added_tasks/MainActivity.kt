@file:Suppress("UNSAFE_CALL_ON_PARTIALLY_DEFINED_RESOURCE")

package com.example.repeatingalarmfoss.screens.added_tasks

import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.commit
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.base.BaseActivity
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.helper.extensions.PREF_LAUNCH_NEVER_SHOW
import com.example.repeatingalarmfoss.helper.extensions.getBooleanOf
import com.example.repeatingalarmfoss.helper.extensions.getDefaultSharedPreferences
import com.example.repeatingalarmfoss.helper.extensions.getAppLaunchCounter
import com.example.repeatingalarmfoss.screens.logs.LogActivity
import com.example.repeatingalarmfoss.screens.settings.SettingsFragment
import com.squareup.seismic.ShakeDetector
import kotlinx.android.synthetic.main.activity_main.*

private const val LAUNCH_COUNTER_THRESHOLD = 5

class MainActivity : BaseActivity(), SetupAddingTaskFragment.TimeSettingCallback, TaskAddedCallback, ShakeDetector.Listener {
    private lateinit var taskListFragment: TaskListFragment
    private lateinit var setupAddingTaskFragment: SetupAddingTaskFragment
    private var isTablet = false
    private val shakeDetector: ShakeDetector by lazy { ShakeDetector(this) }

    override fun onResume() = super.onResume().also { shakeDetector.start(getSystemService(SENSOR_SERVICE) as SensorManager) }
    override fun onPause() = super.onPause().also { shakeDetector.stop() }

    override fun onCreate(savedInstanceState: Bundle?) {
        isTablet = resources.getBoolean(R.bool.isTablet)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        taskListFragment = TaskListFragment.newInstance(this@MainActivity)
        setupAddingTaskFragment = SetupAddingTaskFragment.newInstance(this@MainActivity)
        val settingsFragment = SettingsFragment()
        if (isTablet) {
            supportFragmentManager.commit {
                replace(R.id.detailFragmentContainer, taskListFragment)
                replace(R.id.fragmentContainer, setupAddingTaskFragment)
            }
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
            pager.adapter = MainScreenViewPagerAdapter(this, supportFragmentManager)
            bottomBar.setupWithViewPager(pager)
        }

        if (getDefaultSharedPreferences().getAppLaunchCounter() % LAUNCH_COUNTER_THRESHOLD == 0 && getDefaultSharedPreferences().getBooleanOf(PREF_LAUNCH_NEVER_SHOW).not()) RateMyAppDialog().show(supportFragmentManager, RateMyAppDialog::class.java.simpleName)
    }

    override fun onTimeSet(description: String, repeatingClassifier: RepeatingClassifier, repeatingClassifierValue: String, time: String) = taskListFragment.onTimeSet(description, repeatingClassifier, repeatingClassifierValue, time)
    override fun onSuccessfulScheduling() = if (isTablet) setupAddingTaskFragment.setFieldsDefault() else Unit
    override fun hearShake() = startActivity(Intent(this, LogActivity::class.java))
}

/*todo: flavor ||| notification management ||| widget ||| article for doze mode \ app standby ||| settings - notification light ||| */
/*todo bug on rotate NPE for button, tests for baseContext, back arrow in settings fragment*/