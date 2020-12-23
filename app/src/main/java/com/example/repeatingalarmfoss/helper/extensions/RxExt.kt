package com.example.repeatingalarmfoss.helper.extensions

import com.example.repeatingalarmfoss.helper.rx.DEFAULT_UI_SKIP_DURATION
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

fun Observable<Unit>.throttleFirst(): Observable<Unit> = compose { it.throttleFirst(DEFAULT_UI_SKIP_DURATION, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()) }
