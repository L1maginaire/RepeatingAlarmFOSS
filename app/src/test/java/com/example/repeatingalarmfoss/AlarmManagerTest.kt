package com.example.repeatingalarmfoss

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.example.repeatingalarmfoss.helper.extensions.set
import com.example.repeatingalarmfoss.receivers.AlarmReceiver
import junit.framework.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class AlarmManagerTest {
    private val context = InstrumentationRegistry.getInstrumentation().context.applicationContext
    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val shadowAlarmManager: ShadowAlarmManager = shadowOf(alarmManager)

    @Test
    fun `setting exact one-time alarm`() {
        assertNull(shadowAlarmManager.nextScheduledAlarm)
        val triggerTime = System.currentTimeMillis()
        alarmManager.set(triggerTime, PendingIntent.getBroadcast(context, 0, Intent(context, AlarmReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
        assertEquals(shadowAlarmManager.nextScheduledAlarm.triggerAtTime, triggerTime)

        alarmManager.cancel(PendingIntent.getBroadcast(context, 0, Intent(context, AlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE))
    }

    @Test
    fun `setting multiple alarms`() {
        assertNull(shadowAlarmManager.nextScheduledAlarm)
        val timeArray = (1L..1000L).shuffled().toList()
        assertFalse(shadowAlarmManager.scheduledAlarms.map { it.triggerAtTime } == timeArray)

        for (i in timeArray.indices) {
            alarmManager.set(timeArray[i], PendingIntent.getBroadcast(context, i, Intent(context, AlarmReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
        }
        assertTrue(shadowAlarmManager.scheduledAlarms.map { it.triggerAtTime }.sorted() == timeArray.sorted())

        timeArray.forEachIndexed { index: Int, _: Long -> alarmManager.cancel(PendingIntent.getBroadcast(context, index, Intent(context, AlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE)) }
        assertNull(shadowAlarmManager.nextScheduledAlarm)
    }
}