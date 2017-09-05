package zhuang.jesse.util;

import com.google.api.client.http.HttpMethods;
import zhuang.jesse.constants.MailChimpConstants;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * HTTP REST API utility methods to communicate with mailchimp.<p>
 */
public class HttpUtils {

    public static void testBasicAuth() {



        try {
            URL url = new URL(MailChimpConstants.API_HOME);
//            String mailchimp_key = new String(Files.readAllBytes(Paths.get("src/main/resources/mailchimp_key")));
            System.out.println(MailChimpConstants.API_KEY);
            String encoding = Base64.getEncoder().encodeToString((MailChimpConstants.USERNAME + ":"
                    + MailChimpConstants.API_KEY).getBytes(StandardCharsets.UTF_8));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(HttpMethods.GET);
            // connection.setDoOutput(true); for post request
            connection.setRequestProperty("Authorization", "Basic " + encoding);
            InputStream content = (InputStream) connection.getInputStream();
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
         testBasicAuth();
    }

}
