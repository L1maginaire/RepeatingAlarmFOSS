package com.example.repeatingalarmfoss.helper

import android.content.ContentResolver
import android.content.Context
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Vibrator
import android.provider.Settings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

class Notifier @Inject constructor(
    @JvmField private val cameraManager: CameraManager? = null,
    private val vibrator: Vibrator?,
    private val audioManager: AudioManager?,
    private val deviceHasFlashFeature: Boolean, /*todo named injection*/
    private val context: Context,
    private val contentResolver: ContentResolver,
    private val logger: FlightRecorder
) {
    private val disposable = CompositeDisposable()
    private var player: MediaPlayer? = null
    private val vibrationPattern = longArrayOf(0, 300, 300, 300)
    private var stroboscopeOn = false

    fun stop() {
        vibrator?.cancel()
        player?.release()
        player = null
        disposable.clear()
    }

    fun start() {
        stop()

        ring()
        enableStroboscope()
    }

    @Suppress("DEPRECATION")
    private fun turnFlashLight(on: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager!!.setTorchMode(cameraManager.cameraIdList[0], on)
        } else {
            val camera = Camera.open().apply {
                parameters = parameters.apply { flashMode = if (on) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF }
            }
            if (on) camera.startPreview() else camera.stopPreview().also { camera.release() }
        }
    }

    private fun enableStroboscope() {
        if (deviceHasFlashFeature) {
            disposable += Observable.interval(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnError { turnFlashLight(false) }
                .doOnDispose { turnFlashLight(false) }
                .subscribe {
                    turnFlashLight(stroboscopeOn)
                    stroboscopeOn = stroboscopeOn.not()
                }
        }
    }

    private fun createPlayer(): MediaPlayer? = try {
        MediaPlayer().apply {
            /*todo run if\else in case of call, DND, smth else?*/
            setDataSource(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            isLooping = true
            setAudioStreamType(AudioManager.STREAM_RING)
        }
    } catch (e: IOException) {
        null.also { logger.w { "Failed to create player for incoming call ringer" } }
    }

    private fun ring() {
        kotlin.runCatching {
            Settings.Global.getInt(contentResolver, "zen_mode")
        }.onSuccess {
            if (it != 1) {
                player = createPlayer()?.apply {
                    try {
                        if (isPlaying.not()) {
                            prepare()
                            start()
                            logger.i { "Playing ringtone now." }
                        } else {
                            logger.w { "Ringtone is already playing." }
                        }
                    } catch (e: IllegalStateException) {
                        logger.e(label = "Notifier.ring()", stackTrace = e.stackTrace)
                    } catch (e: IOException) {
                        logger.e(label = "Notifier.ring(), IOE", stackTrace = e.stackTrace)
                    }
                }
                if (audioManager?.ringerMode != AudioManager.RINGER_MODE_SILENT) {
                    vibrator?.vibrate(vibrationPattern, 0)
                }
            }
        }
    }
}