package com.example.repeatingalarmfoss.screens.added_tasks

import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import com.squareup.seismic.ShakeDetector
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), SetupAddingTaskFragment.TimeSettingCallback, TaskAddedCallback, ShakeDetector.Listener {
    private lateinit var taskListFragment: TaskListFragment
    private lateinit var setupAddingTaskFragment: SetupAddingTaskFragment
    private val clicks = CompositeDisposable()
    override fun onDestroy() = super.onDestroy().also { clicks.clear() }
    private val shakeDetector: ShakeDetector by lazy { ShakeDetector(this) }

    override fun onResume() = super.onResume().also { shakeDetector.start(getSystemService(SENSOR_SERVICE) as SensorManager) }
    override fun onPause() = super.onPause().also { shakeDetector.stop() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        taskListFragment = TaskListFragment.newInstance(this@MainActivity)
        setupAddingTaskFragment = SetupAddingTaskFragment.newInstance(this@MainActivity)

        if (root == null) {
            supportFragmentManager.commit {
                replace(R.id.detailFragmentContainer, taskListFragment)
                replace(R.id.fragmentContainer, setupAddingTaskFragment)
            }
        } else {
            supportFragmentManager.commit {
                replace(R.id.root, taskListFragment)
            }
        }
    }

    override fun onTimeSet(description: String, repeatingClassifier: RepeatingClassifier, repeatingClassifierValue: String, time: String) = taskListFragment.onTimeSet(description, repeatingClassifier, repeatingClassifierValue, time)
    override fun onSuccessfulScheduling() = if (root == null/*fixme "twoPane" field*/) setupAddingTaskFragment.setFieldsDefault() else Unit
    override fun hearShake() = startActivity(Intent(this, LogActivity::class.java))
}