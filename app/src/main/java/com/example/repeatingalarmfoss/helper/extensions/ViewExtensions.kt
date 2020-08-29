package com.example.repeatingalarmfoss.helper.extensions

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment

fun ViewGroup.inflate(layoutId: Int): View = LayoutInflater.from(context).inflate(layoutId, this, false)

fun Fragment.toast(message: String) = context?.toast(message)

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT)
        .apply {
            setGravity(Gravity.CENTER, 0, 0)
        }
        .show()
}

fun Activity.toast(message: String) = applicationContext.toast(message)
