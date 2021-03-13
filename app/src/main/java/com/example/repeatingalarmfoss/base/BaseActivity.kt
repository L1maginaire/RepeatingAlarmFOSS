@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "WrongConstant", "RedundantSamConstructor")

package com.example.repeatingalarmfoss.base

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.RepeatingAlarmApp
import com.example.repeatingalarmfoss.helper.extensions.provideUpdatedContextWithNewLocale
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

open class BaseActivity(@LayoutRes layout: Int) : AppCompatActivity(layout) {
    @Inject lateinit var prefs: SharedPreferences
    protected val subscriptions = CompositeDisposable()
    override fun onDestroy() = super.onDestroy().also { subscriptions.clear() }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as RepeatingAlarmApp).appComponent.inject(this)

        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(prefs.getString(getString(R.string.pref_theme), null)!!.toInt()) /*NPE can be caused by lack of defaultValue in preferences.xml of android:key="@string/pref_theme" */
    }

    override fun attachBaseContext(base: Context) = super.attachBaseContext(base.provideUpdatedContextWithNewLocale())
}