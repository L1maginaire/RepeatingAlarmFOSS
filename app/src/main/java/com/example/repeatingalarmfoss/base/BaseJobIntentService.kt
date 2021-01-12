package com.example.repeatingalarmfoss.base

import android.content.Intent
import androidx.annotation.CallSuper
import androidx.core.app.JobIntentService
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.helper.FlightRecorder
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

open class BaseJobIntentService: JobIntentService() {
    @Inject lateinit var logger: FlightRecorder
    protected val subscriptions = CompositeDisposable()

    @CallSuper
    override fun onDestroy() = super.onDestroy().also { subscriptions.clear() }

    @CallSuper
    override fun onCreate() {
        (applicationContext as RepeatingAlarmApp).appComponent.inject(this)
        super.onCreate()
    }

    @CallSuper
    override fun onHandleWork(intent: Intent) = logger.i { "${this.javaClass.simpleName} onHandleWork()" }
}