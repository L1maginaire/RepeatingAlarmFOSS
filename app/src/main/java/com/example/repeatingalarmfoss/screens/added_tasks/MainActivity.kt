package com.example.repeatingalarmfoss.screens.added_tasks

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.db.RepeatingClassifier
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SetupAddingTaskDialog.TimeSettingCallback {
    private lateinit var taskListFragment: TaskListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        taskListFragment = TaskListFragment()
        if(root == null) {
            supportFragmentManager.commit {
                replace(R.id.detailFragmentContainer, taskListFragment)
                replace(R.id.fragmentContainer, SetupAddingTaskDialog.newInstance(this@MainActivity))
            }
        } else {
            supportFragmentManager.commit {
                replace(R.id.root, taskListFragment)
            }
        }
    }

    override fun onTimeSet(description: String, repeatingClassifier: RepeatingClassifier, repeatingClassifierValue: String, time: String) = taskListFragment.onTimeSet(description, repeatingClassifier, repeatingClassifierValue, time)
}