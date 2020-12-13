package com.example.repeatingalarmfoss.screens.alarm

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.repeatingalarmfoss.NotificationsManager
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.base.BaseActivity
import com.example.repeatingalarmfoss.services.AlarmNotifierService
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_alarm.*
import java.io.IOException
import java.util.concurrent.TimeUnit

const val ALARM_ARG_TITLE = "arg_title"

class AlarmActivity : BaseActivity() {
    private val tag = javaClass.simpleName
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val vibrationPattern = longArrayOf(0, 300, 300, 300)
    private var stroboscopeOn = false

    override fun onDestroy() = super.onDestroy().also {
        vibrator?.cancel()
        player?.release()
        dimScreen()
    }

    private val wakeLock: PowerManager.WakeLock by lazy { (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, "repeatingalarmfoss:ON") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnOnScreen()
        setContentView(R.layout.activity_alarm)
        setupClicks()
        tvTaskTitle.text = intent.extras!!.getString(ALARM_ARG_TITLE)
        ring()
        enableStroboscope()
    }

    @Suppress("DEPRECATION")
    private fun turnFlashLight(on: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val camManager = getSystemService(CAMERA_SERVICE) as CameraManager
            val cameraId: String = camManager.cameraIdList[0]
            camManager.setTorchMode(cameraId, on)
        } else {
            val camera = Camera.open().apply {
                parameters = parameters.apply { flashMode = if (on) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF }
            }
            if (on) camera.startPreview() else camera.stopPreview().also { camera.release() }
        }
    }

    private fun enableStroboscope() {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            clicks += Observable.interval(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { turnFlashLight(false) }
                .doOnDispose { turnFlashLight(false) }
                .subscribe {
                    turnFlashLight(stroboscopeOn)
                    stroboscopeOn = stroboscopeOn.not()
                }
        }
    }

    private fun setupClicks() {
        clicks += cancelButton.clicks()
            .throttleFirst(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                stopService(Intent(this, AlarmNotifierService::class.java))
                finish()
            }
        clicks += Observable.timer(10, TimeUnit.MINUTES)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showMissedAlarmNotification()
                finish()
            }
    }

    private fun showMissedAlarmNotification() {
        val builder = NotificationCompat.Builder(this, NotificationsManager.CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(String.format(getString(R.string.title_you_have_missed_alarm), intent.getStringExtra(ALARM_ARG_TITLE)))
            .setPriority(NotificationCompat.PRIORITY_MAX)
        NotificationManagerCompat.from(this).notify(NotificationsManager.MISSED_ALARM_NOTIFICATION_ID, builder.build())
    }

    private fun createPlayer(): MediaPlayer? = try {
        MediaPlayer().apply {
            /*todo run if\else in case of call, DND, smth else?*/
            setDataSource(this@AlarmActivity, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            isLooping = true
            setAudioStreamType(AudioManager.STREAM_RING)
        }
    } catch (e: IOException) {
        Log.e("", "Failed to create player for incoming call ringer")
        null
    }

    private fun ring() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        kotlin.runCatching {
            Settings.Global.getInt(contentResolver, "zen_mode")
        }.onSuccess {
            if (it != 1) {
                player = createPlayer()?.apply {
                    try {
                        if (isPlaying.not()) {
                            prepare()
                            start()
                            Log.i(tag, "Playing ringtone now.")
                        } else {
                            Log.w(tag, "Ringtone is already playing.")
                        }
                    } catch (e: IllegalStateException) {
                        Log.w(tag, e)
                    } catch (e: IOException) {
                        Log.w(tag, e)
                    }
                }
                if ((getSystemService(AUDIO_SERVICE) as? AudioManager)?.ringerMode != AudioManager.RINGER_MODE_SILENT) {
                    vibrator?.vibrate(vibrationPattern, 0)
                }
            }
        }
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