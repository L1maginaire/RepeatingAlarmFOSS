package com.example.repeatingalarmfoss.helper.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

fun ViewGroup.inflate(layoutId: Int): View = LayoutInflater.from(context).inflate(layoutId, this, false)

fun DialogFragment.show(fragmentManager: FragmentManager) = show(fragmentManager, this::class.java.simpleName)