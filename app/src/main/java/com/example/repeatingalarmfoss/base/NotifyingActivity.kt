package com.example.repeatingalarmfoss.base

import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.annotation.CallSuper
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.helper.Notifier
import javax.inject.Inject

open class NotifyingActivity: BaseActivity() {
    private val wakeLock: PowerManager.WakeLock by lazy { (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, javaClass.simpleName) }

    @Inject
    lateinit var notifier: Notifier

    override fun onDestroy() = super.onDestroy().also {
        notifier.stop()
        dimScreen()
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        (application as RepeatingAlarmApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        turnOnScreen()
        notifier.start()
    }

    @Suppress("DEPRECATION")
    private fun turnOnScreen() {
        wakeLock.acquire(10 * 60 * 1000L)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//            setTurnScreenOn(true)
//            setShowWhenLocked(true)
//            (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).requestDismissKeyguard(this, null)
//        } else {
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//            }
        }
    }

    @Suppress("DEPRECATION")
    private fun dimScreen() {
        wakeLock.release()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//            setTurnScreenOn(false)
//            setShowWhenLocked(false)
//            /*todo how to properly lock?!*/
//        } else {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//            }
        }
    }
}
