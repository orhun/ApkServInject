package binder;

import brut.apktool.Main;
import brut.common.BrutException;

import java.io.*;

public class Binder {

    public String decodeApk(File apkFile){
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PrintStream printStream = System.out;
            System.setOut(new PrintStream(byteArrayOutputStream));
            if(apkFile.exists()) {
                Main.main(new String[]{"d", "-f", apkFile.getPath().trim()});
            }else {
                throw new Exception("No readable apk file found.");
            }
            System.out.flush();
            System.setOut(printStream);
            return byteArrayOutputStream.toString();
        }catch (Exception ex){
            ex.printStackTrace();
            return ex.getMessage();
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
