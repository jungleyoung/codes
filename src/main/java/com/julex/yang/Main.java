package com.julex.yang;

import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Main {
    public static void main(String[] args) {
        if (null == args[0]) {
            System.out.println("pls input file from dir");
        }
        if (null == args[1]) {
            System.out.println("pls input file to dir");
        }
        copyDirs(new File( args[0]),args[1]);
    }

    /**
     * This can be used to decrypt files ，only to make work easier , don't share it and against TCL's rules !
     * @param input 输入文件或者文件夹
     * @param output 输出文件夹路径
     */
    public static void copyDirs(File input, String output){
        File outFile = new File(output );
        if (!outFile.exists()) {
            outFile.mkdirs();
        }
        if (input.isDirectory()) {
            File[] files = input.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    copyDirs(file, output + File.separator + file.getName());
                } else {
                    copyDirs(file,output);
                }
            }
        } else {
            try (FileInputStream inputStream = new FileInputStream(input); FileOutputStream outputStream = new FileOutputStream(output + File.separator + input.getName())) {
                System.out.println("复制["+input.getPath()+"]---->>["+output + File.separator + input.getName()+"]");
                IOUtils.copy(inputStream, outputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}