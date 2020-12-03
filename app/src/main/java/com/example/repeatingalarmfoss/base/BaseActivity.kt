package com.example.repeatingalarmfoss.base

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.repeatingalarmfoss.helper.extensions.PREF_APP_LANG
import com.example.repeatingalarmfoss.helper.extensions.getLocalesLanguage
import com.example.repeatingalarmfoss.helper.extensions.getStringOf
import com.example.repeatingalarmfoss.helper.extensions.provideUpdatedContextWithNewLocale
import io.reactivex.disposables.CompositeDisposable

open class BaseActivity : AppCompatActivity() {
    protected val clicks = CompositeDisposable()
    override fun onDestroy() = super.onDestroy().also { clicks.clear() }

    private lateinit var currentLocale: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLocale = resources.configuration.getLocalesLanguage()
    }

    override fun onRestart() {
        super.onRestart()
        if (PreferenceManager.getDefaultSharedPreferences(this).getStringOf(PREF_APP_LANG).equals(currentLocale).not()) {
            currentLocale = PreferenceManager.getDefaultSharedPreferences(this).getStringOf(PREF_APP_LANG)!!
            recreate()
        }
    }

    override fun attachBaseContext(base: Context) = super.attachBaseContext(base.provideUpdatedContextWithNewLocale())
}