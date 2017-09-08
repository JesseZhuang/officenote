package zhuang.jesse.util;

import com.google.common.io.CharStreams;
import zhuang.jesse.constants.GoogleConstants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;

public class FileUtils {

    public static String readFileToString(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed reading file " + filePath);
        }
    }

    public static String readClassPathFileToString(String resourcePath) {
        try {
//            return new String(Files.readAllBytes(Paths.get(FileUtils.class.getResource(resourcePath).getPath())));
//            no physical file inside jar, need to read it as stream
            InputStream in = FileUtils.class.getResourceAsStream(resourcePath);
            return CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed reading class path file " + resourcePath);
        }
    }

    public static Properties loadProperties(String filePath) {
        try {
            Properties properties = new Properties();
            FileInputStream in = new FileInputStream(filePath);
            properties.load(in);
            in.close();
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Read property file failed for " + filePath);
        }
    }

    public static void writeProperties(String filePath, Properties properties) {
        try {
            FileOutputStream out = new FileOutputStream(filePath);
            properties.store(out, null);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String testFilePath = "io/test.properties";
        Properties properties = loadProperties(testFilePath);
        properties.setProperty("testkey", "value");
        writeProperties(testFilePath, properties);

        properties = loadProperties(GoogleConstants.PROPERTIES_FILE_PATH);
        System.out.println(properties.getProperty("notExistingKey"));
        Enumeration<?> e = properties.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String value = properties.getProperty(key);
            System.out.println("Key : " + key + ", Value : " + value);
        }
    }
}
