package com.example.repeatingalarmfoss.screens.biometric

import android.annotation.SuppressLint
import android.content.DialogInterface
import androidx.fragment.app.FragmentActivity
import com.example.repeatingalarmfoss.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_biometric.*

@SuppressLint("InflateParams")
class BiometricDialog(activity: FragmentActivity, private val biometricCallback: BiometricCallback, title: String, description: String): BottomSheetDialog(activity, R.style.Theme_Design_BottomSheetDialog) {
    init {
        setContentView(layoutInflater.inflate(R.layout.dialog_biometric, null))
        tvTitle.text = title
        tvDescription?.text = description
        setCancelable(false)
    }

    fun updateStatus(status: String) {
        tvStatus.text = status
    }

    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        super.setOnDismissListener(listener)
        biometricCallback.onAuthenticationCancelled()
    }
}