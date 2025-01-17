package com.julex.yang;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class MergeSQL {


    public static String sourceCharset = "gbk"; // 源文件编码
    public static String targetCharset = "utf8"; // 目标文件编码
    public static String output = "D:\\版本\\整合SQL"; // 目标文件路径

    public static List<String> dirKeys = Arrays.asList("ebank", "cms", "bedc", "v7");
    public static List<String> types = Arrays.asList("ddl", "dml", "view");

    public static void main(String[] args) {
        new File(output).mkdirs();
        String input = "D:\\SVNREPO\\uat";
        convert(input);
        merge2Path(input);
    }

    public static void merge2Path(String input) {
        File inputFile = new File(input);
        if (!inputFile.isDirectory()) {
            String inputFileName = inputFile.getName().toLowerCase();
            String parentName = inputFile.getParent();
            for (String type : types) {
                if (inputFileName.contains(type)) {
                    for (String key : dirKeys) {
                        if (parentName.contains(key)) {
                            try (
                                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(input)),targetCharset));
                                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output + File.separator + key + "-" + type + ".sql", true)))) {

                                StringBuilder sb = new StringBuilder("---------------------------------------------------------------------------------------\n" + "---------------------" + inputFileName + "\n");
                                String line = null;
                                while ((line = bufferedReader.readLine()) != null) {
                                    sb.append(line).append("\n");
                                }
                                //一次性写入
                                bufferedWriter.write(sb.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }
        } else {
            File[] files = inputFile.listFiles();
            for (File file : files) {
                merge2Path(file.getPath());
            }
        }
    }

    /**
     * 转换格式
     * @param input
     * @throws IOException
     */
    public static void convert(String input)  {
        File file = new File(input);
        // 如果是文件则进行编码转换，写入覆盖原文件
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                convert(subFile.getPath());
            }
        } else if (file.getName().contains(dirKeys.get(0))||file.getName().contains(dirKeys.get(1))||file.getName().contains(dirKeys.get(2))||file.getName().contains(dirKeys.get(3))) {
            String fileCharsetByICU4J = getFileCharsetByICU4J(file);
            System.out.println(file.getName() + "  " + fileCharsetByICU4J);
            if (!"UTF-8".equals(getFileCharsetByICU4J(file))) {

                File targetFile = new File(file.getPath());
                try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), sourceCharset))) {
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        // 注意写入换行符
                        sb.append(line).append("\n");
                    }
                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(targetFile.toPath()), targetCharset))) {
                        bw.write(sb.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("编码转换:" + file.getName());
            }
            if (isContainBOM(file)) {
                System.out.println(file.getName() + " Found BOM!");
                removeBOM(file);
            }
        }

    }

    public static String getFileCharsetByICU4J(File file) {
        String encoding = null;

        try {
            Path path = Paths.get(file.getPath());
            byte[] data = Files.readAllBytes(path);
            CharsetDetector detector = new CharsetDetector();
            detector.setText(data);
            //这个方法推测首选的文件编码格式
            CharsetMatch match = detector.detect();
            //这个方法可以推测出所有可能的编码方式
            CharsetMatch[] charsetMatches = detector.detectAll();
            if (match == null) {
                return encoding;
            }
            encoding = match.getName();
        } catch (IOException var6) {
            System.out.println(var6.getStackTrace());
        }
        return encoding;
    }

    private static boolean isContainBOM(File file)  {

        boolean result = false;

        byte[] bom = new byte[3];
        try (InputStream is = Files.newInputStream(file.toPath())) {

            // read first 3 bytes of a file.
            is.read(bom);

            // BOM encoded as ef bb bf
            String content = new String(Hex.encodeHex(bom));
            if ("efbbbf".equalsIgnoreCase(content)) {
                result = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void removeBOM(File file){
        byte[] bytes = new byte[0];
        try {
            bytes = Files.readAllBytes(Paths.get(file.getPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ByteBuffer bb = ByteBuffer.wrap(bytes);


        byte[] bom = new byte[3];
        // get the first 3 bytes
        bb.get(bom, 0, bom.length);

        // remaining
        byte[] contentAfterFirst3Bytes = new byte[bytes.length - 3];
        bb.get(contentAfterFirst3Bytes, 0, contentAfterFirst3Bytes.length);

        System.out.println(file.getName()+ " Remove the first 3 bytes, and overwrite the file!");

        // override the same path
        try {
            Files.write(Paths.get(file.getPath()), contentAfterFirst3Bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
