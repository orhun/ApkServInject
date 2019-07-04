# ApkServInject

<a href="https://github.com/KeyLo99/ApkServInject/issues"><img src="https://img.shields.io/github/issues/KeyLo99/ApkServInject.svg"/></a>
<a href="https://github.com/KeyLo99/ApkServInject/pulls"><img src="https://img.shields.io/github/issues-pr/KeyLo99/ApkServInject.svg"/></a>
<a href="https://github.com/KeyLo99/ApkServInject/stargazers"><img src="https://img.shields.io/github/stars/KeyLo99/ApkServInject.svg"/></a>
<a href="https://github.com/KeyLo99/ApkServInject/network"><img src="https://img.shields.io/github/forks/KeyLo99/ApkServInject.svg"/></a>
<a href="https://github.com/KeyLo99/ApkServInject/blob/master/LICENSE"><img src="https://img.shields.io/github/license/KeyLo99/ApkServInject.svg"/></a>

ApkServInject - a tool for injecting (smali) service to Android apk files

```
usage: java -jar ApkServInject.jar [-a APK] [-s SMALI]

 -a,--apk     APK file to inject smali
 -s,--smali   (smali) Service file to inject.
 ```
 
 _This software is not working stable for every Android Package. (esp. for complex applications)_

## TODO(s)

* Regex pattern should be fixed for xml parsing.
* Smali edit part must be changed depending on Android App's SDK version
* Finding EP other than onCreate method feature should be added.
* Main Thread may be seperated with functions for more readable code, but not necessary.

## License

GNU General Public License v3. (see [gpl](https://www.gnu.org/licenses/gpl.txt))






 
