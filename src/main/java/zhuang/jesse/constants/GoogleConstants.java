package zhuang.jesse.constants;

import zhuang.jesse.util.FileUtils;

import java.util.Properties;

public class GoogleConstants {

    public static final String PROPERTIES_FILE_PATH = "io/google.properties";
    public static Properties properties = FileUtils.loadProperties(PROPERTIES_FILE_PATH);

}
