package com.example.repeatingalarmfoss

/* NOTIFICATIONS -- begin */

const val CHANNEL_ALARM = "CHANNEL_ALARM"
const val CHANNEL_MISSED_ALARM = "CHANNEL_MISSED_ALARM"
const val CHANNEL_BATTERY_LOW_ID = "CHANNEL_BATTERY_LOW_ID"
const val MISSED_ALARM_NOTIFICATION_ID = 10001

/* NOTIFICATIONS -- end */

/* SHARED ARGUMENTS -- begin*/

/** Argument object :
 *  @see com.example.repeatingalarmfoss.db.Task.description
 * Shared between:
 * @see com.example.repeatingalarmfoss.receivers.AlarmReceiver
 * @see com.example.repeatingalarmfoss.services.AlarmNotifierService
 * @see com.example.repeatingalarmfoss.screens.alarm.AlarmActivity
 * */
const val ALARM_ARG_TASK_TITLE = "global.constant.TASK_TITLE"

/* SHARED ARGUMENTS -- end*/