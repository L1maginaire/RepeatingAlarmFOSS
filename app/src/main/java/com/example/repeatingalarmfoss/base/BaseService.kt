package com.example.repeatingalarmfoss.base

import android.app.Service
import android.content.Intent
import androidx.annotation.CallSuper
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.helper.FlightRecorder
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

abstract class BaseService: Service() {
    @Inject lateinit var logger: FlightRecorder
    private val subscriptions = CompositeDisposable()

    @CallSuper
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger.i { "${this.javaClass.simpleName} onStartCommand()" }
        return super.onStartCommand(intent, flags, startId)
    }

    @CallSuper override fun onCreate() {
        (applicationContext as RepeatingAlarmApp).appComponent.inject(this)
        logger.i { "${this.javaClass.simpleName} creating..." }
    }

    override fun onDestroy() {
        logger.i { "${this.javaClass.simpleName} destroyed" }
        subscriptions.clear()
    }
}
