# ApkServInject <a href="https://github.com/orhun/ApkServInject/releases"><img src="https://img.shields.io/github/release/orhun/ApkServInject.svg"/></a>

<a href="https://github.com/orhun/ApkServInject/issues"><img src="https://img.shields.io/github/issues/orhun/ApkServInject.svg"/></a>
<a href="https://github.com/orhun/ApkServInject/pulls"><img src="https://img.shields.io/github/issues-pr/orhun/ApkServInject.svg"/></a>
<a href="https://github.com/orhun/ApkServInject/stargazers"><img src="https://img.shields.io/github/stars/orhun/ApkServInject.svg"/></a>
<a href="https://github.com/orhun/ApkServInject/network"><img src="https://img.shields.io/github/forks/orhun/ApkServInject.svg"/></a>
<a href="https://github.com/orhun/ApkServInject/blob/master/LICENSE"><img src="https://img.shields.io/github/license/orhun/ApkServInject.svg"/></a>

```
usage: java -jar ApkServInject.jar [-a APK] [-s SMALI]

 -a,--apk     APK file to inject smali
 -s,--smali   (smali) Service file to inject.
 ```

 ```
     _          _    ____                  ___        _           _   
    / \   _ __ | | _/ ___|  ___ _ ____   _|_ _|_ __  (_) ___  ___| |_ 
   / _ \ | '_ \| |/ \___ \ / _ | '__\ \ / /| || '_ \ | |/ _ \/ __| __|
  / ___ \| |_) |   < ___) |  __| |   \ V / | || | | || |  __| (__| |_ 
 /_/   \_| .__/|_|\_|____/ \___|_|    \_/ |___|_| |__/ |\___|\___|\__|
         |_|@orhun                               |__/               

ApkServInject - a tool for injecting (smali) service to Android apk files

usage: ApkServInject
 -a,--apk     APK file to inject smali
 -s,--smali   (smali) Service file to inject.

For additional info, see: http://github.com/orhun/ApkServInject

Test files found. Would you like to perform a test injection? [y/n]: 
y
[16:29:08] Starting...
[16:29:08] Decompiling APK with ApkTool...
S: WARNING: Could not write to (/home/k3/.local/share/apktool/framework), using /tmp instead...
S: Please be aware this is a volatile directory and frameworks could go missing, please utilize --frame-path 
if the default storage directory is unavailable
[16:29:15] 
I: Using Apktool 2.3.3 on test.apk
I: Loading resource table...
I: Decoding AndroidManifest.xml with resources...
I: Loading resource table from file: /tmp/1.apk
I: Regular manifest package...
I: Decoding file-resources...
I: Decoding values */* XMLs...
I: Baksmaling classes.dex...
I: Copying assets and libs...
I: Copying unknown files...
I: Copying original files...

[16:29:15] Manifest file found: AndroidManifest.xml
[16:29:15] Trying to find EntryPoint smali...
[16:29:15] EntryPoint smali: com.test.testapp.MainActivity
[16:29:15] Changing manifest file...
[16:29:15] Moving and changing smali file...
[16:29:15] Permissions found: 
android.permission.WRITE_EXTERNAL_STORAGE
android.permission.READ_EXTERNAL_STORAGE
android.permission.INTERNET

[16:29:15] Merging permissions...
[16:29:15] Permissions added to manifest.
[16:29:15] Changing EntryPoint smali...
[16:29:15] onCreate method found!
[16:29:15] EntryPoint smali changed.
[16:29:15] Building the APK...
S: WARNING: Could not write to (/home/k3/.local/share/apktool/framework), using /tmp instead...
S: Please be aware this is a volatile directory and frameworks could go missing, 
please utilize --frame-path if the default storage directory is unavailable
[16:29:23] 
I: Using Apktool 2.3.3
I: Checking whether sources has changed...
I: Smaling smali folder into classes.dex...
I: Checking whether resources has changed...
I: Building resources...
I: Building apk file...
I: Copying unknown files/dir...
I: Built apk...

[16:29:24] Signing...
[16:29:25] APK signed ~ done.

[16:29:25] New APK located at "/home/k3/t3mp/ApkServInject/test_x.apk"
[16:29:25] Clearing workspace...
```
 
 _This software is not working stable for every Android Package. (esp. for complex applications)_

## TODO(s)

* Regex pattern should be fixed for xml parsing.
* Smali edit part must be changed depending on Android Apps SDK version.
* Finding EP other than onCreate method feature should be added.
* Main Thread may be seperated to functions for more readable code, but not necessary.

## License

GNU General Public License v3. (see [gpl](https://www.gnu.org/licenses/gpl.txt))