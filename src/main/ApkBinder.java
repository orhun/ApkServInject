package main;

import binder.Binder;
import java.io.File;

public class ApkBinder {
    private static String workingDir = System.getProperty("user.dir");
    private static Binder binder;
    public static void main(String[] args) {
        /*Get these values from args*/
        File apkFile = new File(workingDir + "\\test.apk");
        File smaliFile = new File(workingDir + "\\MyService.smali");
        /*==========================*/
        binder = new Binder();
        binder.bindApk(apkFile, smaliFile);
    }

}
