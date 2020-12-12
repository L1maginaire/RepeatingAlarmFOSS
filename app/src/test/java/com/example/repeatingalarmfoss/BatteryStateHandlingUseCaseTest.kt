package com.example.repeatingalarmfoss

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.Build
import com.example.repeatingalarmfoss.helper.FlightRecorder
import com.example.repeatingalarmfoss.helper.extensions.PREF_LOW_BATTERY_DND_AT_NIGHT
import com.example.repeatingalarmfoss.helper.extensions.activityImplicitLaunch
import com.example.repeatingalarmfoss.receivers.BATTERY_THRESHOLD_PERCENTAGE
import com.example.repeatingalarmfoss.repositories.PermissionToNotifyAboutLowBatteryResult
import com.example.repeatingalarmfoss.repositories.PreferencesRepository
import com.example.repeatingalarmfoss.screens.low_battery.LowBatteryNotifierActivity
import com.example.repeatingalarmfoss.services.LowBatteryNotificationService
import com.example.repeatingalarmfoss.usecases.BatteryStateHandlingUseCase
import com.nhaarman.mockitokotlin2.argumentCaptor
import io.reactivex.Single
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class BatteryStateHandlingUseCaseTest {
    private val context: Context = mock(Context::class.java)
    private val batteryManager = mock(BatteryManager::class.java)
    private val repo = mock(PreferencesRepository::class.java)
    private val logger = mock(FlightRecorder::class.java)
    private val batteryStateHandlingUseCase = BatteryStateHandlingUseCase(context, batteryManager, logger, repo)

    @Test
    fun `when allowed to show low battery screen and battery level is more than threshold - LowBatteryNotifierActivity shouldn't launch`() {
        `when`(repo.isForbiddenToNotifyLowBatteryAtNight()).thenReturn(Single.just(PermissionToNotifyAboutLowBatteryResult.Success(true)))
        `when`(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).thenReturn(BATTERY_THRESHOLD_PERCENTAGE.inc())
        batteryStateHandlingUseCase.execute()
        verify(logger, times(1)).i(argumentCaptor<Boolean>().capture(), argumentCaptor<() -> String>().capture())
        com.nhaarman.mockitokotlin2.verify(context, com.nhaarman.mockitokotlin2.times(0)).activityImplicitLaunch(LowBatteryNotificationService::class.java, LowBatteryNotifierActivity::class.java)
    }

    @Test
    fun `when is forbidden to show low battery screen - LowBatteryNotifierActivity shouldn't launch`() {
        `when`(repo.isForbiddenToNotifyLowBatteryAtNight()).thenReturn(Single.just(PermissionToNotifyAboutLowBatteryResult.Success(false)))
        batteryStateHandlingUseCase.execute()
        com.nhaarman.mockitokotlin2.verify(batteryManager, com.nhaarman.mockitokotlin2.times(0)).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        com.nhaarman.mockitokotlin2.verify(context, com.nhaarman.mockitokotlin2.times(0)).activityImplicitLaunch(LowBatteryNotificationService::class.java, LowBatteryNotifierActivity::class.java)
    }

    @Test
    fun `when Android API version lesser than 21, BatteryManager shouldn't be injected in class, and therefore, we should rely on IntentFilter`() {
        val batteryStateHandlingUseCase = BatteryStateHandlingUseCase(context, null, logger, repo)
        `when`(repo.isForbiddenToNotifyLowBatteryAtNight()).thenReturn(Single.just(PermissionToNotifyAboutLowBatteryResult.Success(true)))
        `when`(context.registerReceiver(nullable(BroadcastReceiver::class.java), com.nhaarman.mockitokotlin2.any())).thenReturn(Intent().apply {
            putExtra(BatteryManager.EXTRA_LEVEL, 100)
            putExtra(BatteryManager.EXTRA_SCALE, 100)
        })
        batteryStateHandlingUseCase.execute()
        verify(logger, times(1)).i(argumentCaptor<Boolean>().capture(), argumentCaptor<() -> String>().capture())
    }

    @Test
    fun `when we rely on IntentFilter, and value of battery level below threshold, Activity shouldn't launch`() {
        val batteryStateHandlingUseCase = BatteryStateHandlingUseCase(context, null, logger, repo)
        `when`(repo.isForbiddenToNotifyLowBatteryAtNight()).thenReturn(Single.just(PermissionToNotifyAboutLowBatteryResult.Success(true)))
        `when`(context.registerReceiver(nullable(BroadcastReceiver::class.java), com.nhaarman.mockitokotlin2.any())).thenReturn(Intent().apply {
            putExtra(BatteryManager.EXTRA_LEVEL, BATTERY_THRESHOLD_PERCENTAGE.dec())
            putExtra(BatteryManager.EXTRA_SCALE, BATTERY_THRESHOLD_PERCENTAGE.dec())
        })
        batteryStateHandlingUseCase.execute()
        verify(logger, times(1)).i(argumentCaptor<Boolean>().capture(), argumentCaptor<() -> String>().capture())
        com.nhaarman.mockitokotlin2.verify(context, com.nhaarman.mockitokotlin2.times(0)).activityImplicitLaunch(LowBatteryNotificationService::class.java, LowBatteryNotifierActivity::class.java)
    }

    @Test
    fun `when we rely on IntentFilter and value of battery level above threshold, Activity should launch`() {
        `when`(context.activityImplicitLaunch(LowBatteryNotificationService::class.java, LowBatteryNotifierActivity::class.java)).then {}
        val batteryStateHandlingUseCase = BatteryStateHandlingUseCase(context, null, logger, repo)
        `when`(repo.isForbiddenToNotifyLowBatteryAtNight()).thenReturn(Single.just(PermissionToNotifyAboutLowBatteryResult.Success(true)))
        `when`(context.registerReceiver(nullable(BroadcastReceiver::class.java), com.nhaarman.mockitokotlin2.any())).thenReturn(Intent().apply {
            putExtra(BatteryManager.EXTRA_LEVEL, BATTERY_THRESHOLD_PERCENTAGE)
            putExtra(BatteryManager.EXTRA_SCALE, BATTERY_THRESHOLD_PERCENTAGE)
        })
        batteryStateHandlingUseCase.execute()
        verify(logger, times(1)).i(argumentCaptor<Boolean>().capture(), argumentCaptor<() -> String>().capture())
        com.nhaarman.mockitokotlin2.verify(context, com.nhaarman.mockitokotlin2.times(1)).activityImplicitLaunch(LowBatteryNotificationService::class.java, LowBatteryNotifierActivity::class.java)
    }

    @Test
    fun `repository test - if there's no DND restrictions - any time is suitable for showing the screen`() {
        val sp = mock(SharedPreferences::class.java)
        `when`(sp.getBoolean(PREF_LOW_BATTERY_DND_AT_NIGHT, true)).thenReturn(false)
        PreferencesRepository(sp)
            .isForbiddenToNotifyLowBatteryAtNight("00:00" /** ANY time can be placed here*/).test().assertNoErrors().assertComplete().assertResult(PermissionToNotifyAboutLowBatteryResult.Success(true))
    }

    @Test
    fun `repository test - if there are DND restrictions and time is between 0 and 9 AM - do not show the screen`() {
        val sp = mock(SharedPreferences::class.java)
        `when`(sp.getBoolean(PREF_LOW_BATTERY_DND_AT_NIGHT, true)).thenReturn(true)
        PreferencesRepository(sp)
            .isForbiddenToNotifyLowBatteryAtNight("00:00").test().assertNoErrors().assertComplete().assertResult(PermissionToNotifyAboutLowBatteryResult.Success(false))
    }

    @Test
    fun `repository test - if there are DND restrictions and time is between 9 and 0 AM - show the screen`() {
        val sp = mock(SharedPreferences::class.java)
        `when`(sp.getBoolean(PREF_LOW_BATTERY_DND_AT_NIGHT, true)).thenReturn(true)
        PreferencesRepository(sp)
            .isForbiddenToNotifyLowBatteryAtNight("23:59").test().assertNoErrors().assertComplete().assertResult(PermissionToNotifyAboutLowBatteryResult.Success(true))
    }
}
