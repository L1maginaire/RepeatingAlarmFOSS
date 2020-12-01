package com.example.repeatingalarmfoss.base

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.repeatingalarmfoss.helper.extensions.provideUpdatedContextWithNewLocale
import io.reactivex.disposables.CompositeDisposable

open class BaseActivity : AppCompatActivity() {
    protected val clicks = CompositeDisposable()
    override fun onDestroy() = super.onDestroy().also { clicks.clear() }

    override fun attachBaseContext(base: Context) = super.attachBaseContext(base.provideUpdatedContextWithNewLocale())
}