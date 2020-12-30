package com.example.repeatingalarmfoss.screens.low_battery

import android.content.Intent
import android.os.Bundle
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.base.ForegroundService
import com.example.repeatingalarmfoss.base.NotifyingActivity
import com.example.repeatingalarmfoss.helper.extensions.throttleFirst
import com.example.repeatingalarmfoss.services.LowBatteryNotificationService
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_low_battery_notifier.*
import java.util.concurrent.TimeUnit

class LowBatteryNotifierActivity : NotifyingActivity() {
    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).apply {
        setContentView(R.layout.activity_low_battery_notifier)
        subscriptions += Observable.timer(10, TimeUnit.MINUTES, AndroidSchedulers.mainThread())
            .subscribe { shutdownNotifiers() }
        subscriptions += buttonGotIt.clicks()
            .throttleFirst()
            .subscribe { shutdownNotifiers() }
    }

    private fun shutdownNotifiers() {
        startService(Intent(this@LowBatteryNotifierActivity, LowBatteryNotificationService::class.java).apply {
            action = ForegroundService.ACTION_TERMINATE
        })
        finish()
    }
}