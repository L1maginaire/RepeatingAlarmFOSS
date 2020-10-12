package com.example.repeatingalarmfoss.screens

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import android.provider.Settings
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.repeatingalarmfoss.NotificationsManager
import com.example.repeatingalarmfoss.R
import java.io.IOException

class NotifierService : Service() {
    companion object {
        const val TAG = "NotifierService"
        const val ID = 101
        const val TERMINATE = "action_terminate"
        const val ARG_TASK_TITLE = "arg_task_title"
    }

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val vibrationPattern = longArrayOf(0, 300, 300, 300)

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
                            Log.i(TAG, "Playing ringtone now.")
                        } else {
                            Log.w(TAG, "Ringtone is already playing.")
                        }
                    } catch (e: IllegalStateException) {
                        Log.w(TAG, e)
                    } catch (e: IOException) {
                        Log.w(TAG, e)
                    }
                }
                if ((getSystemService(AUDIO_SERVICE) as? AudioManager)?.ringerMode != AudioManager.RINGER_MODE_SILENT) {
                    vibrator?.vibrate(vibrationPattern, 0)
                }
            }
        }
    }

    override fun onDestroy() = super.onDestroy().also {
        vibrator?.cancel()
        player?.release()
    }

    private fun createPlayer(): MediaPlayer? = try {
        MediaPlayer().apply {
            /*todo run if\else in case of call, DND, smth else?*/
            setDataSource(this@NotifierService, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            isLooping = true
            setAudioStreamType(AudioManager.STREAM_RING)
        }
    } catch (e: IOException) {
        Log.e("", "Failed to create player for incoming call ringer")
        null
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY.also {
        val taskTitle = intent?.getStringExtra(ARG_TASK_TITLE) ?: throw IllegalArgumentException()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel(NotificationsManager.CHANNEL_CALLS_ID, NotificationsManager.CALLS)
        val notificationBuilder = NotificationCompat.Builder(this, NotificationsManager.CHANNEL_CALLS_ID)
            .setSmallIcon(R.drawable.ic_notification_ringing)
            .setOngoing(true)
            .setContentText(taskTitle)
            .addAction(getCancelAction())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(getFullscreenIntent(taskTitle), true)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_notification_ringing))
        startForeground(ID, notificationBuilder.build())

        ring()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String) {
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE).apply {
            lightColor = Color.BLUE
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
//            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), AudioAttributes.Builder()
//                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
//                .build())
        }
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(chan)
    }

    private fun getFullscreenIntent(taskTitle: String): PendingIntent = PendingIntent.getActivity(
        this,
        0,
        Intent(this, AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(ARG_TASK_TITLE, taskTitle)
        },
        0
    )

    private fun getCancelAction(): NotificationCompat.Action = NotificationCompat.Action(
        0,
        SpannableString(getString(R.string.dismiss)).apply {
            setSpan(ForegroundColorSpan(Color.RED), 0, getString(R.string.dismiss).length, 0)
        },
        PendingIntent.getService(this, 0, Intent(this, NotifierService::class.java).apply { this.action = TERMINATE }, PendingIntent.FLAG_UPDATE_CURRENT)
    )
}
