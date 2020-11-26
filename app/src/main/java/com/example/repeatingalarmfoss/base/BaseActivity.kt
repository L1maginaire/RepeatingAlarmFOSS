package com.example.repeatingalarmfoss.base

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable

open class BaseActivity: AppCompatActivity() {
    protected val clicks = CompositeDisposable()
    override fun onDestroy() = super.onDestroy().also { clicks.clear() }
}