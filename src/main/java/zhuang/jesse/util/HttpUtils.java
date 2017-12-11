package zhuang.jesse.util;

import com.google.api.client.http.HttpMethods;
import zhuang.jesse.constants.MailChimpConstants;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;

/**
 * HTTP REST API utility methods to communicate with mailchimp.<p>
 */
public class HttpUtils {

    public static void testBasicAuth() {

        try {
            URL url = new URL(MailChimpConstants.API_HOME);
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

    /**
     * 403 forbidden status response from Edmonds School District website
     *
     * @param urlAddress
     * @return page html source
     */
    @Deprecated
    public static String getHtmlFromUrlSimple(String urlAddress) {
        String htmlContent = "";
        try {
            htmlContent = new Scanner(new URL(urlAddress).openStream(), "UTF-8").useDelimiter("\\A").next();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return htmlContent;
    }

    public static String getHtmlFromUrl(String urlAddress) {
        URL urlObject = null;
        try {
            urlObject = new URL(urlAddress);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URLConnection urlConnection = null;
        try {
            urlConnection = urlObject.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 " +
                "(KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(urlConnection.getInputStream(), "UTF-8")))
        {
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputLine = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(inputLine);
            }

            return stringBuilder.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "crawl html failed";
    }

    public static void main(String[] args) {
        testBasicAuth();
    }

}
