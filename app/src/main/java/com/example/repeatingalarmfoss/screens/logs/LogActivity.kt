package com.example.repeatingalarmfoss.screens.logs

import android.os.Bundle
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.base.BaseActivity
import com.example.repeatingalarmfoss.helper.rx.DEFAULT_UI_SKIP_DURATION
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_log.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LogActivity : BaseActivity() {
    @Inject
    lateinit var logger: FlightRecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as RepeatingAlarmApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        supportActionBar?.title = this::class.java.simpleName
        setContentView(R.layout.activity_log)
        logsContainer.text = logger.getEntireRecord()
        clicks += eraseLogButton
            .clicks()
            .throttleFirst(DEFAULT_UI_SKIP_DURATION, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe { logger.clear().also { logsContainer.text = "" } }
    }
}