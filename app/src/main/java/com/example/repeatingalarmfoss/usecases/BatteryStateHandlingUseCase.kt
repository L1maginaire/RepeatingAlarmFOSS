package com.example.repeatingalarmfoss.usecases

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.annotation.VisibleForTesting
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.activityImplicitLaunch
import com.example.repeatingalarmfoss.helper.extensions.toReadableDate
import com.example.repeatingalarmfoss.repositories.PermissionToNotifyAboutLowBatteryResult
import com.example.repeatingalarmfoss.repositories.PreferencesRepository
import com.example.repeatingalarmfoss.screens.low_battery.LowBatteryNotifierActivity
import com.example.repeatingalarmfoss.services.LowBatteryNotificationService
import javax.inject.Inject

@VisibleForTesting const val BATTERY_THRESHOLD_PERCENTAGE = 30

class BatteryStateHandlingUseCase @Inject constructor(private val context: Context, @JvmField private val batteryManager: BatteryManager? = null, private val logger: FlightRecorder, private val preferencesRepository: PreferencesRepository) {
    @SuppressLint("NewApi")
    fun execute() {
        preferencesRepository.isForbiddenToNotifyLowBatteryAtNight()
            .filter { it is PermissionToNotifyAboutLowBatteryResult.Success && it.permitted }
            /** blockingGet() for Maybe.empty() which is result of filtering by permission returns null*/
            .blockingGet()
            ?.apply {
                (batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { intentFilter ->
                    context.registerReceiver(null, intentFilter)?.let { intent ->
                        val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        level * 100 / scale.toFloat()
                    }
                }?.toInt())?.also {
                    logger.i { "Battery level is $it" }
                    if (it < BATTERY_THRESHOLD_PERCENTAGE) {
                        context.activityImplicitLaunch(LowBatteryNotificationService::class.java, LowBatteryNotifierActivity::class.java)
                    }
                }
            }
    }
}