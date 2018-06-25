package main;

import binder.Binder;
import java.io.File;

public class ApkBinder {
    private static String workingDir = System.getProperty("user.dir");
    private static Binder binder;
    public static void main(String[] args) {
        binder = new Binder();

        /*binder.decodeApk(new File(workingDir+"\\testfile.apk"), (output1) -> {
            System.out.println(output1);
            System.out.println("=============");
            binder.buildApk(new File(workingDir+"\\testfile\\"), (output2) -> {
                System.out.println(output2);
                System.out.println("=============");
            });
        });*/
        binder.signApk(new File(workingDir+"\\testfile.apk"));
    }
}
