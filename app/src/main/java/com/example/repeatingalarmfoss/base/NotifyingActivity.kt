package com.example.repeatingalarmfoss.base

import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.annotation.CallSuper
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.helper.Notifier
import com.example.repeatingalarmfoss.helper.extensions.LongExt.minutesToMilliseconds
import javax.inject.Inject

open class NotifyingActivity : BaseActivity() {
    @Inject
    lateinit var wakeLock: PowerManager.WakeLock

    @Inject
    lateinit var keyguardManager: KeyguardManager

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        wakeLock.acquire(minutesToMilliseconds(10))
    }

    @Suppress("DEPRECATION")
    private fun dimScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        wakeLock.release()
    }
}
