# Feature: obfuscation with custom dictionary.

[ProGuard](https://www.guardsquare.com/en/products/proguard/manual/introduction) is the tool which can ([among other things]((https://developer.android.com/studio/build/shrink-code))) shrink package\class\variables\methods names before assembling .dex file. 
**proguard-rules.pro** is the config file for ProGuard. Following options provide custom dictionaries for replacing fields\methods names, classes and packages names respectively:
-obfuscationdictionary some_dictionary.txt
-classobfuscationdictionary some_dictionary.txt
-packageobfuscationdictionary some_dictionary.txt

For example, we can take [Java language keywords list](https://android.googlesource.com/platform/prebuilts/tools/+/tools_r17/common/proguard/proguard4.7/examples/dictionaries/keywords.txt) for that purpose.

To ensure everything works fine we should decompile our .apk file with [apktool](https://ibotpeaches.github.io/Apktool/): 
```apktool d -r -s app-debug.apk```
Do not forget to turn on obfuscation for flavor you're gonna use to build apk. For example, for debug flavor we should declare in app's build.gradle such things:
```
debug {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
```
Then, build .apk file (__Build -> Build Bundle(s) \ APK(s) -> Build APK(s)__). On success you'll find .apk file in __YourAppName/app/build/outputs/apk/debug__ directory.
Decompile application, and after you'll get __classes.dex__ file, you'll need to decompile .dex file. [JD-GUI](https://github.com/java-decompiler/jd-gui) can help with this, just run the program and open classes.dex file:
![jd-gui-screenshot](images/jd-gui-decompilation-example.png)

By the way, [jadx-gui](https://github.com/skylot/jadx) has workaround for the issue with java keywords in namings:
(![jadx-screenshot]jadx-gui-decompilation-example.png)
Program detects keywords in not appropriate use and adds, let's say salt, to all these namings.
