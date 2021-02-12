## Feature: Forced Dark Theme

Since Android 10 (API level 29) and higher there is system-wide dark theme available. To follow device dark mode you only need to inherit your core theme from ```Theme.MaterialComponents.DayNight.*```. For example:

**styles.xml**:
```    <style name="Theme.MyApplication" parent="Theme.MaterialComponents.DayNight.DarkActionBar">```

**AndroidManifest.xml**:
```    <application
           ...
           android:theme="@style/Theme.MyApplication">
```

System will handle the rest.

Otherwise, if you want to ignore system dark mode changes, you can inherit from ```Light```:
```    <style name="Theme.MyApplication" parent="Theme.MaterialComponents.Light.DarkActionBar">```

or, programmatically:
    * disable for the whole application:

        ```AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)```

    * disable for a single Activity:
        ```delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        After calling this you probably need to call recreate() for this to take effect```

To override colors in dark mode you should create directory named **values-night** inside **res** directory with **colors.xml** file inside with dark mode colors specified inside.

To force app-wide dark mode you should have controller (in this test case it will be CheckBox) and some persistence (for test case it will be [SharedPreferences](https://developer.android.com/training/data-storage/shared-preferences)):
```
    clicks += checkbox
        .checkedChanges()
        .skipInitialValue()
        .subscribe { isChecked ->
            prefs.putBoolean(darkModePrefId, isChecked)
            recreate()
        }
```

then, in ancestor of **all** activities declare such code:
```
open class BaseActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val isDarkModeEnabled = prefs.getBoolean(darkModePrefId)
        AppCompatDelegate.setDefaultNightMode(if (isDarkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        ...
    }
}
```

If you have other activities alive in stack they will handle theme changes accordingly and automatically (don't forget to save the state if necessary).