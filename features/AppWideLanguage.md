## Feature: App-wide language.

Beware! This approach [might be buggy](https://stackoverflow.com/a/2900144), but anyway, it seems, there's no alternatives to it.

Android doesn't provide any built-in mechanism to change particular application's language.

```attachBaseContext(base: Context)``` function is calling before ```onCreate()```, it is much earlier context's initialization procedure. **Context**-based components rely on [Locale class](https://developer.android.com/reference/java/util/Locale) in case which language to use on particular screen, and therefore, we obliged to provide appropriate locale before every context-based component's initialization.

For first, we need to override ```attachBaseContext()``` in **Application** subclass:
```
class MyApp: Application() {
    ...
    override fun attachBaseContext(base: Context) = super.attachBaseContext(base.provideUpdatedContextWithNewLocale())
}
```
Let's say, we have **Activity** which is parent for every **Activity** we have in application:
```
class BaseActivity: AppCompatActivity() {
    ...  
    override fun attachBaseContext(base: Context) = super.attachBaseContext(base.provideUpdatedContextWithNewLocale())	
}
```

[Extension function](https://kotlinlang.org/docs/reference/extensions.html) provides (based on persisted value) proper **Locale** to **Context**:
```
fun Context.provideUpdatedContextWithNewLocale(
    persistedLanguage: String? = kotlin.runCatching { getDefaultSharedPreferences().getStringOf(PREF_APP_LANG) }.getOrNull(),
    defaultLocale: String = Locale.getDefault().language
): Context {
    val locales = resources.getStringArray(R.array.supported_locales)
    val newLocale = Locale(locales.firstOrNull { it == persistedLanguage || it == defaultLocale } ?: Locale.UK.language)
    getDefaultSharedPreferences().writeStringOf(PREF_APP_LANG, newLocale.language)
    Locale.setDefault(newLocale)
    return createConfigurationContext(Configuration().apply { setLocale(newLocale) })
}
```

Good practice will be also to observe device's language preference change and persist it's value:
```
override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig
    @Suppress("DEPRECATION")
    getDefaultSharedPreferences().writeStringOf(PREF_APP_LANG, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                                                    newConfig.locales[0].language
                                                                    else
                                                                    newConfig.locale.language)
}
```

Moreover,```BaseActivity``` should also be always ready to reflect changes in app-wide language. If we have something like ```SettingsActivity``` where actual value of persisted language changing, code might be the following:
```
open class BaseActivity : AppCompatActivity() {
    ...
    private lateinit var currentLocale: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        currentLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        resources.configuration.locales[0].language
                        else
                        resources.configuration.locale.language
    }

    override fun onRestart() {
        super.onRestart()
        if (PreferenceManager.getDefaultSharedPreferences(this).getStringOf(PREF_APP_LANG).equals(currentLocale).not()) {
            currentLocale = PreferenceManager.getDefaultSharedPreferences(this).getStringOf(PREF_APP_LANG)!!
            recreate()
        }
    }
}
```

