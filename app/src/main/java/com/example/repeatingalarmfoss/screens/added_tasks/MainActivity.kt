package com.example.repeatingalarmfoss.screens.added_tasks

import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.commit
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.base.BaseActivity
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.example.repeatingalarmfoss.screens.logs.LogActivity
import com.example.repeatingalarmfoss.screens.settings.SettingsFragment
import com.squareup.seismic.ShakeDetector
import kotlinx.android.synthetic.main.activity_main.*

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
        val settingsContainer: Int
        val settingsFragment = SettingsFragment()
        if (isTablet) {
            settingsContainer = R.id.detailFragmentContainer
            supportFragmentManager.commit {
                replace(R.id.detailFragmentContainer, taskListFragment)
                replace(R.id.fragmentContainer, setupAddingTaskFragment)
            }
        } else {
            settingsContainer = R.id.root
            supportFragmentManager.commit {
                replace(R.id.root, taskListFragment)
            }
        }
        bottomBar.apply {
//            setupWithViewPager(mainViewPager)
            onTabSelected = {
                supportFragmentManager.commit {
                    replace(
                        settingsContainer, when (it.id) {
                            R.id.tab_alarm -> taskListFragment
                            R.id.tab_settings -> settingsFragment
                            else -> throw IllegalStateException()
                        }
                    )
                }
            }
        }
    }

    override fun onTimeSet(description: String, repeatingClassifier: RepeatingClassifier, repeatingClassifierValue: String, time: String) = taskListFragment.onTimeSet(description, repeatingClassifier, repeatingClassifierValue, time)
    override fun onSuccessfulScheduling() = if (isTablet) setupAddingTaskFragment.setFieldsDefault() else Unit
    override fun hearShake() = startActivity(Intent(this, LogActivity::class.java))
}

/*todo: flavor ||| rate us ||| notification management ||| widget ||| article for doze mode \ app standby ||| settings - notification light ||| */
/*todo bug on rotate NPE for button, tests for baseContext, back arrow in settings fragment*/