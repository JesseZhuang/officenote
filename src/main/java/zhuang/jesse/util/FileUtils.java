package zhuang.jesse.util;

import com.google.api.client.util.Charsets;
import com.google.api.client.util.IOUtils;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
}
