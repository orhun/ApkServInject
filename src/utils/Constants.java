package utils;

import java.util.regex.Pattern;

public final class Constants {
    /*// TODO: 6/26/2018 Declare custom exceptions */
    public final String APK_NOT_FOUND_ERROR = "[!] No readable apk file found.";
    public final String SMALI_NOT_FOUND_ERROR = "[!] No readable apk or smali file found.";
    public final String MANIFEST_NOT_FOUND_ERROR = "[!] Manifest file not found";
    public final String MANIFEST_PARSE_ERROR = "[!] Manifest parse error.";
    public final String EP_SMALI_NOT_FOUND_ERROR = "[!] EntryPoint Smali not found.";

    public final Pattern manifestPattern1 =  Pattern.compile("<activity(.+?)android:name=\"(.+?)\">(.+?)<action android:name=\"android.intent.action.MAIN\"\\/>(.+?)<\\/activity>");


}
