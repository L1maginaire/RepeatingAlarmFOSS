package com.example.repeatingalarmfoss.screens

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.repeatingalarmfoss.R
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_alarm.*
import java.util.concurrent.TimeUnit

class AlarmActivity : AppCompatActivity() {
    private val clicks = CompositeDisposable()
    override fun onDestroy() = super.onDestroy().also {
        dimScreen()
        clicks.clear()
    }

    private val wakeLock: PowerManager.WakeLock by lazy { (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, "repeatingalarmfoss:ON") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnOnScreen()
        setContentView(R.layout.activity_alarm)
        clicks += cancelButton.clicks()
            .throttleFirst(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                stopService(Intent(this, NotifierService::class.java))
                finish()
            }
    }

    @Suppress("DEPRECATION")
    private fun turnOnScreen() {
        wakeLock.acquire(10 * 60 * 1000L)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
            (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).requestDismissKeyguard(this, null)
        } else {
            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
                addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
                addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun dimScreen() {
        wakeLock.release()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(false)
            setShowWhenLocked(false)
            /*todo how to properly lock?!*/
        } else {
            window.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
                clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
                clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
}