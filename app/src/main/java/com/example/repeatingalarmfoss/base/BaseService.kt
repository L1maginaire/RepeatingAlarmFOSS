package com.example.repeatingalarmfoss.base

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.annotation.CallSuper
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.helper.FlightRecorder
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

const val ID_LOW_BATTERY_NOTIFICATIONS_SERVICE = 1001
const val ID_ALARM_NOTIFIER_SERVICE = 1002
const val ID_NEXT_LAUNCH_PREPARING_SERVICE = 1003
const val ID_RESCHEDULING_ALARMS_ON_BOOT_SERVICE = 1004

abstract class BaseService: Service() {
    @Inject lateinit var logger: FlightRecorder
    protected val subscriptions = CompositeDisposable()

    @CallSuper
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger.i { "${this.javaClass.simpleName} onStartCommand()" }
        return super.onStartCommand(intent, flags, startId)
    }

    @CallSuper override fun onCreate() {
        (applicationContext as RepeatingAlarmApp).appComponent.inject(this)
        logger.i { "${this.javaClass.simpleName} creating..." }
    }

    @CallSuper
    override fun onDestroy() {
        logger.i { "${this.javaClass.simpleName} destroyed" }
        subscriptions.clear()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
