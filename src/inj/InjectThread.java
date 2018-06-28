package inj;

import utils.Constants;
import utils.exceptions.*;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public class InjectThread extends Thread {
    private File apkFile, smaliFile;
    private File manifestFile, epSmaliFile, newSmaliFile, newApkFile, signedApkFile;
    private String manifestContent, androidPackageName, epFilePath, smaliPackageName,
            serviceElement, newSmaliFileContent, epSmaliFileContent, onCreateLine,
            serviceStarterSmali, onCreateMethod, oldMethod, permissions, manifestTag, androidPermissions;
    private Matcher manifestMatcher, manifestTagMatcher;
    private int onCreateLineNum;
    private Constants constants = new Constants();
    private static Injector inj = new Injector();
    
    public InjectThread(File apkFile, File smaliFile){
        this.apkFile = apkFile;
        this.smaliFile = smaliFile;
    }
    @Override
    public void run() { /* Main proc */
        /* 0- Decompile
         * 1- Edit Manifest file -> Add service tags
         * 2- Edit service package
         * 3- Edit Smali file -> Start service
         * 4- Build and sign
         * */
        inj.printLog("Starting...");
        inj.printLog("Decompiling APK with ApkTool...");
        if(apkFile.exists() && smaliFile.exists()){
            inj.decompileApk(apkFile, output1 -> {
                try {
                    inj.printLog("\n" + output1);
                    if(output1.split("I:")[output1.split("I:").length-1].contains("Copying original files...")) {  /* Check last info */
                        manifestFile = new File(apkFile.getParent() + "\\" + inj.getFilenameWithoutExtension(apkFile.getName()) + "\\AndroidManifest.xml");
                        if (!manifestFile.exists()) { throw new ManifestNotFoundException(constants.MANIFEST_NOT_FOUND_ERROR + " : \"" + manifestFile.getPath() + "\""); }
                        inj.printLog("Manifest file found: " + manifestFile.getName());
                        inj.printLog("Trying to find EntryPoint smali...");
                        manifestContent = inj.readFile(manifestFile.getPath()).replace("\n", "").replace("\r", "");
                        manifestMatcher = constants.manifestPattern1.matcher(manifestContent);
                        if (manifestMatcher.find()){
                            androidPackageName = manifestMatcher.group(0).replaceAll("<activity(.+?)android:name=\"", "")
                                    .replaceAll("\"(.+?)<intent-filter>(.+?)+", "");
                        }
                        inj.printLog("EntryPoint smali: " + androidPackageName);
                        if(androidPackageName.length()<5 | androidPackageName.split("[.]").length == 0){ throw new FileParseException(constants.MANIFEST_PARSE_ERROR);}
                        epFilePath = manifestFile.getParent() + "\\smali";
                        for(int i = 0; i < androidPackageName.split("[.]").length-1; i++){
                            epFilePath += "\\" + androidPackageName.split("[.]")[i];
                        }
                        epFilePath += "\\" + androidPackageName.split("[.]")[androidPackageName.split("[.]").length-1] + ".smali";
                        epSmaliFile = new File(epFilePath);
                        if(!epSmaliFile.exists()){throw new SmaliNotFoundException(constants.EP_SMALI_NOT_FOUND_ERROR);}
                        /*Edit Manifest*/
                        inj.printLog("Changing manifest file...");
                        smaliPackageName = androidPackageName.replace("."+ androidPackageName.split("[.]")[androidPackageName.split("[.]").length-1], "") + "." +
                                inj.getFilenameWithoutExtension(smaliFile.getName());
                        serviceElement = "<service android:enabled=\"true\" android:exported=\"true\" android:name=\""+ smaliPackageName + "\"/>";
                        if(!inj.writeFile(manifestFile.getPath(), manifestContent.replace(manifestMatcher.group(0), manifestMatcher.group(0)+serviceElement))){
                            throw new FileOperationException(constants.FILE_WRITE_ERROR);}
                        /*Copy smali and edit package*/
                        inj.printLog("Moving and changing smali file...");
                        newSmaliFile = new File(epSmaliFile.getParent()+"\\"+smaliFile.getName());
                        if(!inj.copyFile(smaliFile.getPath(), newSmaliFile.getPath())){ throw new FileOperationException(constants.FILE_COPY_ERROR);}
                        String smaliPackage = inj.readFile(newSmaliFile.getPath()).split("\n")[0].replace(".class public L", "").replace(";", "");
                        newSmaliFileContent = inj.readFile(newSmaliFile.getPath()).replaceAll(".class(.+?)L(.+?)/(.+?);", ".class public L"+
                                smaliPackageName.replace(".", "/")+";").replace(smaliPackage, smaliPackageName.replace(".", "/"));
                        if(!inj.writeFile(newSmaliFile.getPath(), newSmaliFileContent)){throw new FileOperationException(constants.FILE_WRITE_ERROR);}
                        /*Add permissions*/
                        if(newSmaliFileContent.contains("# [PERMISSIONS]")){
                            permissions = newSmaliFileContent.split("# \\[PERMISSIONS\\]")[1].replace("#", "");
                            if(permissions.contains("android.") | permissions.length() > 7){
                                inj.printLog("Permissions found: " + permissions);
                                inj.printLog("Merging permissions...");
                                manifestContent = inj.readFile(manifestFile.getPath());
                                manifestTag = "";
                                androidPermissions = "";
                                manifestTagMatcher = constants.manifestTagPattern.matcher(manifestContent);
                                while (manifestTagMatcher.find()){
                                    manifestTag = manifestTagMatcher.group();
                                }
                                if(manifestTag.length()<5){throw new FileParseException(constants.MANIFEST_PARSE_ERROR);}
                                for(String perm:permissions.split("\n")){
                                    if (perm.length()>5) {
                                        androidPermissions += "<uses-permission android:name=\"" + perm + "\"/>";
                                    }
                                }
                                if(!inj.writeFile(manifestFile.getPath(), manifestContent.replace(manifestTag, manifestTag+androidPermissions))){
                                    throw new FileOperationException(constants.FILE_WRITE_ERROR + ": " + manifestFile.getPath());}
                                if(!inj.writeFile(newSmaliFile.getPath(), newSmaliFileContent.split("# \\[PERMISSIONS\\]")[0])){
                                    throw new FileOperationException(constants.FILE_WRITE_ERROR + ": " + newSmaliFile.getPath());}
                                inj.printLog("Permissions added to manifest.");
                            }
                        }
                        /*Edit EP File*/
                        inj.printLog("Changing EntryPoint smali...");
                        epSmaliFileContent = inj.readFile(epSmaliFile.getPath());
                        if(!epSmaliFileContent.contains(".method protected onCreate")){ throw new EpNotFoundException(constants.ONCREATE_NOT_FOUND_ERROR);}
                        inj.printLog("onCreate method found!");
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
                            }catch (Exception e){/*Cannot convert*/}
                        }
                        inj.printLog("Building the APK...");
                        if(!inj.writeFile(epSmaliFile.getPath(), epSmaliFileContent.replace(oldMethod, onCreateMethod))){ throw new FileOperationException(constants.FILE_WRITE_ERROR);}
                        /*Build and Sign APK*/
                        inj.buildApk(new File(apkFile.getParent() + "\\" + inj.getFilenameWithoutExtension(apkFile.getName())), output2 -> {
                            try {
                                inj.printLog("\n" + output2);
                                if (output2.split("I:")[output2.split("I:").length - 1].contains("Built apk...")) {  /* Check last info */
                                    newApkFile = new File(apkFile.getParent() + "\\" + inj.getFilenameWithoutExtension(apkFile.getName()) + "\\dist\\" + apkFile.getName());
                                    if(!newApkFile.exists()){throw new ApkNotFoundException(constants.NEW_APK_NOT_FOUND_ERROR);}
                                    TimeUnit.SECONDS.sleep(1);
                                    inj.printLog("Signing...");
                                    inj.signApk(newApkFile, output3 -> {
                                        try {
                                            if (output3.equals("[+] Apk signed.")) {
                                                signedApkFile = new File(newApkFile.getParent() + "\\" + inj.getFilenameWithoutExtension(newApkFile.getName())+".s.apk");
                                                if(!signedApkFile.exists()){throw new ApkSignException(constants.APK_SIGN_ERROR);}
                                                if(!inj.copyFile(signedApkFile.getPath(), apkFile.getParent() + "\\" +
                                                        inj.getFilenameWithoutExtension(apkFile.getName()) + "_x.apk")){throw new FileOperationException(constants.FILE_COPY_ERROR);}
                                                /* DONE */
                                                inj.printLog("APK signed. ~ done");
                                                System.out.println();
                                                inj.printLog("New APK located at \"" + apkFile.getParent() + "\\" +
                                                        inj.getFilenameWithoutExtension(apkFile.getName()) + "_x.apk" + "\"");
                                                inj.clearWorkspace(apkFile);
                                            } else {
                                                throw new ApkSignException(constants.APK_SIGN_ERROR);
                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    });
                                } else {
                                    throw new Exception(output2);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        });

                    }else{
                        throw new Exception(output1);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }else{
            System.out.println(constants.SMALI_NOT_FOUND_ERROR);
        }
    }
}
