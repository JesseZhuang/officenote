package zhuang.jesse.eflier;

import zhuang.jesse.constants.EflierConstants;
import zhuang.jesse.entity.Eflier;
import zhuang.jesse.util.HttpUtils;
import zhuang.jesse.util.TimeUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EflierCrawler {

    private static final String START_IDENTIFIER = "<li>";
    private static final String END_IDENTIFIER = "</li>";
    private static final String URL_IDENTIFIER = "<a href=\"";

    public List<Eflier> crawlAllEfliers() {
        List<Eflier> efilers = new ArrayList<>();
        List<String> pageUrls = EflierConstants.EFLIER_URLS;
        for (String pageUrl : pageUrls) {
            efilers.addAll(crawlEflierOnPage(pageUrl));
        }
        return efilers;
    }

    private List<Eflier> crawlEflierOnPage(String pageUrl) {
        List<Eflier> efilers = new ArrayList<>();
        String eflierSection = getEflierSection(pageUrl);
        int startIndex = 0, endIndex;
        LocalDate lastMonday = TimeUtils.getLastMonday();
        while (startIndex >= 0) {
            endIndex = findIndexOrThrowException(eflierSection, END_IDENTIFIER, startIndex);
//            System.out.println("jesse " + endIndex);
            String eflierLine = getOneEflierLine(eflierSection, startIndex, endIndex);
//            System.out.println("jesse " + eflierLine);

            String postedDateString = getPostedDate(eflierLine);

            LocalDate postedDate = TimeUtils.parseDate(postedDateString);
            if (postedDate != null && postedDate.isBefore(lastMonday)) break;

            efilers.add(new Eflier(getEflierTitle(eflierLine), getEflierDownloadUrl(eflierLine),
                    Optional.ofNullable(postedDateString)));
            startIndex = eflierSection.indexOf(START_IDENTIFIER, endIndex);
//            System.out.println("jesse "+ startIndex);
        }

        return efilers;
    }

    private String getEflierSection(String pageUrl) {
        String htmlContent = HttpUtils.getHtmlFromUrl(pageUrl);
        int startIndex = getListStartIndex(htmlContent);
        int endIndex = getListEndIndex(htmlContent, startIndex);
//        System.out.println("start index " + startIndex);
//        System.out.println("end index " + endIndex);
        return htmlContent.substring(startIndex, endIndex);
    }

    private String getOneEflierLine(String fliers, int startIndex, int endIndex) {
        return fliers.substring(findIndexOrThrowException(fliers, START_IDENTIFIER, startIndex)
                + START_IDENTIFIER.length(), endIndex);
    }

    private String getEflierTitle(String eflierLine) {
        final String plainText = eflierLine.substring(0, findIndexOrThrowException(eflierLine, URL_IDENTIFIER));
        String downloadUrlText;
        final String urlTitleIdentifier = "title=\"";
        int startIndex = findIndexOrThrowException(eflierLine, urlTitleIdentifier) + urlTitleIdentifier.length();
        int endIndex = findIndexOrThrowException(eflierLine, "\"", startIndex);
        downloadUrlText = eflierLine.substring(startIndex, endIndex);
        return plainText + downloadUrlText;
    }

    private String getEflierDownloadUrl(String eflierLine) {
        int startIndex = findIndexOrThrowException(eflierLine, URL_IDENTIFIER) + URL_IDENTIFIER.length();
        int endIndex = findIndexOrThrowException(eflierLine, "\"", startIndex);
        String downloadUrl = eflierLine.substring(startIndex, endIndex).replace(" ", "%20");
        return EflierConstants.ESD_DOMAIN + downloadUrl;
    }

    private String getPostedDate(String eflierLine) {
//        System.out.println("jesse " + eflierLine);
        int index = findIndexOrThrowException(eflierLine, "posted");
//        System.out.println("jesse " + index);
        String datePart = eflierLine.substring(index);
        Matcher matcher = Pattern.compile("\\d+/\\d+/\\d+").matcher(datePart);
        if (matcher.find()) {
//            System.out.println("jesse " + eflierLine.substring(matcher.start()));
//            return eflierLine.substring(index + matcher.start());
            return matcher.group();
        } else {
//            throw new RuntimeException("Cannot find eflier posted date in " + eflierLine);
            return null;
        }
    }

    private int getListStartIndex(String htmlContent) {
        final String listStartIdentifier = "<ul>    <li>";
        return findIndexOrThrowException(htmlContent, listStartIdentifier);
    }

    private int getListEndIndex(String htmlContent, int listStartIndex) {
        final String listEndIdentifier = "</li></ul>";
        return findIndexOrThrowException(htmlContent, listEndIdentifier, listStartIndex) + END_IDENTIFIER.length();
    }

    private int findIndexOrThrowException(String text, String pattern) {
        int result = text.indexOf(pattern);
        if (result != -1) {
            return result;
        } else {
            throw new RuntimeException("Cannot find " + pattern + " in text:\n" + text);
        }
    }

    private int findIndexOrThrowException(String text, String pattern, int startIndex) {
        int result = text.indexOf(pattern, startIndex);
        if (result != -1) {
            return result;
        } else {
            throw new RuntimeException("Cannot find " + pattern + " in text:\n" + text);
        }
    }

    public static void main(String[] args) {
        EflierCrawler crawler = new EflierCrawler();
        for (String pageUrl : EflierConstants.EFLIER_URLS) {
//            System.out.println("[debug]: for pageUrl "+ pageUrl);
//
            System.out.println(crawler.getEflierSection(pageUrl));
            System.out.println("----------");
            for (Eflier flier : crawler.crawlEflierOnPage(pageUrl)) System.out.println(flier);
            System.out.println("------");
        }
//        System.out.println(crawler.crawlAllEfliers());

//        System.out.println("tes".indexOf("t"));
//        System.out.println("tes".indexOf("t", 0));

    }
}
