package zhuang.jesse.constants;

import zhuang.jesse.util.FileUtils;
import zhuang.jesse.util.TimeUtils;

public class MailChimpConstants {

    public static final String API_HOME = "https://us1.api.mailchimp.com/3.0/";
    public static final String USERNAME = "dummyUserName";
    public static final String EMAIL_SUBJECT = "Office Notes *|DATE:M d Y|*";
    public static final String CAMPAIGN_TYPE = "regular";
    public static final String TO_NAME = "*|FNAME|* *|LNAME|*";
    public static final String CAMPAIGN_TITLE = "Office Notes "
            + TimeUtils.getComingMonday().format(TimeUtils.CAMPAIGN_TITLE);

    public static final String API_KEY = FileUtils.readClassPathFileToString("/mailchimp_key");
    public static final String CALENDAR_STICKY = FileUtils.readFileToString("io/mailchimp_calendar_sticky.html");
    public static final String PART1 = FileUtils.readClassPathFileToString("/mailchimp_template_part1.html");
    public static final String PART2 = FileUtils.readClassPathFileToString("/mailchimp_template_part2.html");
    public static final String PART3 = FileUtils.readClassPathFileToString("/mailchimp_template_part3.html");

    public static String mailchimpLeftColumn;
    public static String mailchimpRightColumn;
    public static String mailchimpWhole;
    public static String googleDoc;

    public static void main(String[] args) {
        System.out.println(API_KEY);
    }

}
