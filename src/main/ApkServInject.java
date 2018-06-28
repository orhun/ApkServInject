package main;

import inj.Injector;
import org.apache.commons.cli.*;
import utils.Constants;
import utils.exceptions.ApkNotFoundException;
import utils.exceptions.ApkServInjectException;

import java.io.File;
import java.util.Scanner;

public class ApkServInject {
    private static String workingDir = System.getProperty("user.dir");
    private static final Injector injector = new Injector();
    private static final Options options = new Options();
    private static final File testApkFile = new File(workingDir + "/test.apk");
    private static final File testSmaliFile = new File(workingDir + "/Serv2Inject.smali");
    private static Option apkFileOption, smaliFileOption;
    private static Boolean testInj = false;
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        createOptions();
        try {
            CommandLine commandLine = parser.parse(options, args, false);
            if(commandLine.hasOption("a") && commandLine.hasOption("apk") &&
                    commandLine.hasOption("s") && commandLine.hasOption("smali")){
                File apkFile = new File(commandLine.getArgs()[0]);
                File smaliFile = new File(commandLine.getArgs()[1]);
                if(!apkFile.exists()){throw new ApkNotFoundException(new Constants().APK_NOT_FOUND_ERROR);}
                if(!smaliFile.exists()){throw new ApkNotFoundException(new Constants().SMALI_NOT_FOUND_ERROR);}
                if(apkFile.getParent() == null || smaliFile.getParent() == null){ /* Same dir */
                    apkFile = new File(workingDir + "/" + apkFile.getName());
                    smaliFile = new File(workingDir + "/" + smaliFile);
                    System.out.println(apkFile.getPath() + smaliFile.getParent());
                }
                printHeader();
                injector.injectSmali(apkFile, smaliFile);
            }else{
                throw new ApkServInjectException("Print_usage");
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            printUsage();
            testInj = true;
        }catch (ApkServInjectException e){
            printUsage();
            testInj = true;
        }catch (ApkNotFoundException e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        if(testInj) {
            try {
                if (testApkFile.exists() && testSmaliFile.exists()) {
                    Scanner reader = new Scanner(System.in);
                    System.out.println("Test files found. Would you like to perform a test injection? [y/n]: ");
                    String answer = reader.next();
                    if (answer.toLowerCase().trim().equals("y")) {
                        injector.injectSmali(testApkFile, testSmaliFile);
                    } else {
                        injector.printLog("Exiting...");
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
    private static void createOptions() {
        apkFileOption = Option.builder("a").longOpt("apk").desc("APK file to inject smali").build();
        smaliFileOption = Option.builder("s").longOpt("smali").desc("(smali) Service file to inject.").build();
        options.addOption(apkFileOption);
        options.addOption(smaliFileOption);
    }
    private static void printHeader(){
        System.out.println(new Constants().asciiHeader);
        System.out.println("ApkServInject - a tool for injecting (smali) service to Android apk files\n");
    }
    private static void printUsage(){
        printHeader();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ApkServInject", options);
        System.out.println("\nFor additional info, see: http://github.com/KeyLo99/ApkServInject\n");
    }
}
