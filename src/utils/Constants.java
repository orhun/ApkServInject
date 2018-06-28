package utils;

import java.util.regex.Pattern;

public final class Constants {

    public final String APK_NOT_FOUND_ERROR = "[!] No readable APK file found.";
    public final String SMALI_NOT_FOUND_ERROR = "[!] No readable APK or smali file found.";
    public final String MANIFEST_NOT_FOUND_ERROR = "[!] Manifest file not found";
    public final String MANIFEST_PARSE_ERROR = "[!] Manifest parse error.";
    public final String EP_SMALI_NOT_FOUND_ERROR = "[!] EntryPoint Smali not found.";
    public final String FILE_WRITE_ERROR = "[!] File cannot be written.";
    public final String FILE_COPY_ERROR = "[!] Cannot copy file.";
    public final String ONCREATE_NOT_FOUND_ERROR = "[!] onCreate method not found in EntryPoint smali.";
    public final String NEW_APK_NOT_FOUND_ERROR = "[!] No built APK file found.";
    public final String APK_SIGN_ERROR = "[!] APK not signed.";
    public final Pattern manifestPattern1 =  Pattern.compile("<activity(.+?)android:name=\"(.+?)\">(.+?)<action android:name=\"android.intent.action.MAIN\"\\/>(.+?)<category android:name=\"android.intent.category.LAUNCHER\"\\/>(.+?)<\\/activity>");
    public final Pattern manifestTagPattern = Pattern.compile("<manifest(.+?)>");
    public final String service_starter_smali = ".line <line_num>\n" +
            "    new-instance v0, Landroid/content/Intent;\n" +
            "\n" +
            "    invoke-virtual {p0}, L<class_name>;->getApplicationContext()Landroid/content/Context;\n" +
            "\n" +
            "    move-result-object v1\n" +
            "\n" +
            "    const-class v2, L<service_name>;\n" +
            "\n" +
            "    invoke-direct {v0, v1, v2}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V\n" +
            "\n" +
            "    invoke-virtual {p0, v0}, L<class_name>;->startService(Landroid/content/Intent;)Landroid/content/ComponentName;";
    public final String asciiHeader = "\n" +
            "     _          _    ____                  ___        _           _   \n" +
            "    / \\   _ __ | | _/ ___|  ___ _ ____   _|_ _|_ __  (_) ___  ___| |_ \n" +
            "   / _ \\ | '_ \\| |/ \\___ \\ / _ | '__\\ \\ / /| || '_ \\ | |/ _ \\/ __| __|\n" +
            "  / ___ \\| |_) |   < ___) |  __| |   \\ V / | || | | || |  __| (__| |_ \n" +
            " /_/   \\_| .__/|_|\\_|____/ \\___|_|    \\_/ |___|_| |__/ |\\___|\\___|\\__|\n" +
            "         |_|@KeyLo99                               |__/               \n";
}
