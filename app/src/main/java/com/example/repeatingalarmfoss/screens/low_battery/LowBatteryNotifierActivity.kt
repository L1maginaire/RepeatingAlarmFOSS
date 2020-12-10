package com.example.repeatingalarmfoss.screens.low_battery

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.repeatingalarmfoss.R

class LowBatteryNotifierActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).apply { setContentView(R.layout.activity_low_battery_notifier) }
}