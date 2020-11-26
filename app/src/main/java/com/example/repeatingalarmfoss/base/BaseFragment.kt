package com.example.repeatingalarmfoss.base

import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable

open class BaseFragment: Fragment() {
    val clicks = CompositeDisposable()
    override fun onDestroyView() = super.onDestroyView().also { clicks.clear() }
}