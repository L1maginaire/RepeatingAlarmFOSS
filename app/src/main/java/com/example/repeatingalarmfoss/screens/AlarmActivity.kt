package com.example.repeatingalarmfoss.screens

import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.example.repeatingalarmfoss.R
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_alarm.*
import java.util.concurrent.TimeUnit

class AlarmActivity : AppCompatActivity() {
    private val clicks = CompositeDisposable()
    override fun onDestroy() = super.onDestroy().also { clicks.clear() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        clicks += cancelButton.clicks()
            .throttleFirst(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                stopService(Intent(this, NotifierService::class.java))
                finish()
            }
    }
}