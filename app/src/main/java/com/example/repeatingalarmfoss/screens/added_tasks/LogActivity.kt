package com.example.repeatingalarmfoss.screens.added_tasks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.screens.alarm.ALARM_ARG_TITLE
import kotlinx.android.synthetic.main.activity_log.*
import javax.inject.Inject

class LogActivity : AppCompatActivity() {
    @Inject
    lateinit var logger: FlightRecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as RepeatingAlarmApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        supportActionBar?.title = this::class.java.simpleName
        setContentView(R.layout.activity_log)
        logsContainer.text = logger.getEntireRecord()
    }
}