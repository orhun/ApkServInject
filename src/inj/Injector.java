package inj;

import brut.apktool.Main;
import org.apache.commons.io.FileUtils;
import s.Sign;
import utils.*;
import utils.exceptions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public class Injector {
    public interface ConsoleProcess{
        void onFinish(String output);
    }
    private ConsoleProcess consoleProcess;
    private Constants constants;

    public Injector(){
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
                throw new ApkNotFoundException(constants.APK_NOT_FOUND_ERROR);
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
                throw new ApkNotFoundException(constants.APK_NOT_FOUND_ERROR);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
    public void injectSmali(File apkFile, File smaliFile){
        new InjectThread(apkFile, smaliFile).run();
    }
    public void clearWorkspace(File apkFile){
        try {
            printLog("Clearing workspace...");
            File apktDir = new File(apkFile.getParent() + "\\" + getFilenameWithoutExtension(apkFile.getName()) + "\\");
            if(!apktDir.exists()){ throw new Exception("There is nothing to delete"); }
            FileUtils.deleteDirectory(apktDir);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public String readFile(String filePath){
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
    public boolean writeFile(String filePath, String content){
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
    public boolean copyFile(String src, String dst){
        try {
            Files.copy(Paths.get(src), Paths.get(dst), StandardCopyOption.REPLACE_EXISTING);
            return true;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }
    public void printLog(String msg){
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[" + dateFormat.format(new Date()) + "] " + msg);
    }
    public String getFilenameWithoutExtension(String filename){
        return filename.replaceFirst("[.][^.]+$", "");
    }
}
