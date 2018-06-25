package main;

import binder.Binder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ApkBinder {
    private static String workingDir = System.getProperty("user.dir");
    private static Binder binder;
    public static void main(String[] args) {
        binder = new Binder();
        String logs = binder.decodeApk(new File(workingDir+"\\testfile.apk"));
        System.out.println(logs);
    }
}
