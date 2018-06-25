package binder;

import brut.apktool.Main;
import java.io.*;

public class Binder {
    public interface ConsoleProcess{
        void onFinish(String output);
    }
    private ConsoleProcess consoleProcess;

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
                throw new Exception("No readable apk file found.");
            }
            System.out.flush();
            System.setOut(printStream);
            consoleProcess.onFinish(byteArrayOutputStream.toString());
        }catch (Exception ex){
            ex.printStackTrace();
            consoleProcess.onFinish(ex.getMessage());
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
                }
                in.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return content.toString();
    }

}
