package com.example.repeatingalarmfoss.screens.added_tasks

import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.repeatingalarmfoss.R
import com.example.repeatingalarmfoss.helper.extensions.PREF_LAUNCH_NEVER_SHOW
import com.example.repeatingalarmfoss.helper.extensions.getDefaultSharedPreferences
import com.example.repeatingalarmfoss.helper.extensions.writeBooleanOf

class RateMyAppDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog.Builder(requireContext())
        .setIcon(R.drawable.ic_baseline_star_rate_24)
        .setNeutralButton(R.string.title_dont_ask_againg) { _, _ -> requireContext().getDefaultSharedPreferences().writeBooleanOf(PREF_LAUNCH_NEVER_SHOW, true) }
        .setTitle(R.string.title_rate_our_app)
        .setPositiveButton(R.string.title_yes) { _, _ ->
            val appPackageName: String = requireActivity().packageName
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (anfe: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
        }
        .setNegativeButton(R.string.title_no, null)
        .create()
}