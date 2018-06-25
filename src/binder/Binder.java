package binder;

import brut.apktool.Main;
import orig.SignApk;
import s.Sign;
import utils.*;

import java.io.*;
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
    public void signApk(File apkFile){
        try {
            if(apkFile.exists()) {
                Sign.main(new String[]{apkFile.getPath(), "--override"});
            }else{
                throw new Exception(constants.APK_NOT_FOUND_ERROR);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
    public void bindApk(File apkFile, File smaliFile){

        if(apkFile.exists() && smaliFile.exists()){
            decodeApk(apkFile, output1 -> {
                try {
                    if(output1.split("I:")[output1.split("I:").length-1].contains("Copying original files...")) {  /* Check last info */
                        File manifestFile = new File(apkFile.getParent() + "\\" + getFilenameWithoutExtension(apkFile.getName()) + "\\AndroidManifest.xml");
                        System.out.println(manifestFile.getPath());
                        if (!manifestFile.exists()) { throw new Exception(constants.MANIFEST_NOT_FOUND_ERROR); }
                        String manifestContent = readFile(manifestFile.getPath()).replace("\n", "").replace("\r", "");
                        Matcher manifestMatcher = constants.manifestPattern1.matcher(manifestContent);
                        String androidName = "";
                        if (manifestMatcher.find()){
                            androidName = manifestMatcher.group(0).replace("<activity android:name=\"", "").replaceAll("\">(.+?)<intent-filter>(.+?)+", "");
                        }
                        if(androidName.length()<5 | androidName.split("[.]").length == 0){ throw new Exception(constants.MANIFEST_PARSE_ERROR);}
                        String epFilePath = manifestFile.getParent() + "\\smali";
                        for(int i = 0; i < androidName.split("[.]").length-1; i++){
                            epFilePath += "\\" + androidName.split("[.]")[i];
                        }
                        epFilePath += "\\" + androidName.split("[.]")[androidName.split("[.]").length-1] + ".smali";
                        File epSmaliFile = new File(epFilePath);
                        if(!epSmaliFile.exists()){throw new Exception(constants.EP_SMALI_NOT_FOUND_ERROR);}
                        System.out.println(epSmaliFile.getPath());
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
                }
                in.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return content.toString();
    }
    private String getFilenameWithoutExtension(String filename){
        return filename.replaceFirst("[.][^.]+$", "");
    }

}
