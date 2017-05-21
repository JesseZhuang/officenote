package zhuang.jesse.personal;


import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadGmail {
    private final static String HOST = "imap.gmail.com";
    private final static String USERNAME = "madronaofficenotes@gmail.com";

  /*
   * do not pull events for now, may implement with google calendar API later.
   * 02/05/16.
   */
    // private LinkedList<CalendarEvent> events;

    private static int findStartInd(String wholeMsg, String startHeader,
                                    int startInd, int length) {
        int ans = wholeMsg.indexOf(startHeader, startInd);
        ans += length;
        return ans;
    }

    public static List<Blurb> fetchBlurbs(String password) {

        final String TARGET1 = "Submission Form";
        final String TARGET2RE = ".*(?i)fl[yi]ers.*";
        final String CONTENT_TAG1 = "<font style=\"font-family: "
                + "sans-serif; font-size:12px;\">";
        final int CONTENT_HEADER_LEN = CONTENT_TAG1.length();
        final String CONTENT_TAG2 = "</font>";
        final String TITLE_HEADER1 = "<strong>Article Title:</strong>";
        final String TITLE_HEADER2 = "How long should article be printed?";
        final String TITLE_HEADER3 = "<strong>Article Contents:</strong>";
        final String TITLE_HEADER4 = "Upload Photo, Flyer, etc:";
        // final String TITLE_HEADER5 = "Is this an Event?";

        Properties props = new Properties();
        props.setProperty("mail.imap.ssl.enable", "true");
        // set any other needed mail.imap.* properties here
        Session session = Session.getInstance(props);

        // ArrayList should be faster and smaller than LinkedList
        List<Blurb> blurbs = new ArrayList<Blurb>();

        try {
            // need to set to allow less secure apps in gmail
            Store store = session.getStore("imap");
            // on linux mint with maven javax.mail.mail 1.4 version the line below hangs forever
            store.connect(HOST, USERNAME, password);

            Folder inbox = store.getFolder("INBOX");
            if (inbox == null) {
                System.out.println("Cannot get INBOX folder.");
                System.exit(1);
            }
            inbox.open(Folder.READ_ONLY);
            int totalMessageCounts = inbox.getMessageCount();

            if (totalMessageCounts == 0) {
                System.out.println("Empty inbox folder.");
                inbox.close(false);
                store.close();
                System.exit(1);
            }

            // start parsing blurbs, note numbering start from 1
            Blurb eflier = null;
            for (int i = 1; i <= totalMessageCounts; i++) {
                Message msg = inbox.getMessage(i);
                String msgContent;
                String msgSubject = msg.getSubject();

                if (msgSubject.contains(TARGET1)) {

                    msgContent = (String) msg.getContent();
                    // pull blurb title
                    int startInd = msgContent.indexOf(TITLE_HEADER1);
                    startInd = findStartInd(msgContent, CONTENT_TAG1, startInd,
                            CONTENT_HEADER_LEN);

                    String title = msgContent.substring(startInd,
                            msgContent.indexOf(CONTENT_TAG2, startInd));

                    // pull blurb time length
                    startInd = msgContent.indexOf(TITLE_HEADER2, startInd);
                    startInd = findStartInd(msgContent, CONTENT_TAG1, startInd,
                            CONTENT_HEADER_LEN);

                    int howManyWeeks = Integer.parseInt(msgContent.substring(startInd,
                            msgContent.indexOf(" Week", startInd)));
                    // pull blurb content
                    startInd = msgContent.indexOf(TITLE_HEADER3, startInd);
                    startInd = findStartInd(msgContent, CONTENT_TAG1, startInd,
                            CONTENT_HEADER_LEN);

                    String content = msgContent.substring(startInd,
                            msgContent.indexOf(CONTENT_TAG2, startInd));
                    // delete line carriage returns and extra html line breaks
                    // • word bullet sign
                    // system line separator is "\r\n" on windows 10

                    // remove line returns, extra <br>s, extra white spaces

                    // content = content.replace(System.lineSeparator(), "")
                    // need to use \r\n since the email message read has \r\n even when read in linux, either through
                    // jar file on command line or IntelliJ Idea in Linux mint, 2017-02-06
                    content = content.replace("\r\n", "")
                            .replaceAll("((<br />)\\s*){2,}", "<br />")
                            .replaceAll("\\s{2,}", " ").replace("·", "•");

                    // if flyer link is available, pull and add the blurb
                    if (msgContent.contains(TITLE_HEADER4)) {
                        startInd = msgContent.indexOf(TITLE_HEADER4);
                        startInd = findStartInd(msgContent, "<ul><li>", startInd,
                                "<ul><li>".length());

                        String flyerLink = msgContent
                                .substring(startInd,
                                        msgContent.indexOf("</li></ul>" + CONTENT_TAG2, startInd))
                                .replaceAll(System.lineSeparator(), "")
                                .replace("</li><li>", " and ");

                        blurbs.add(new Blurb(title, content, 1, howManyWeeks, flyerLink));
                    } else blurbs.add(new Blurb(title, content, 1, howManyWeeks));
                } else if (msgSubject.matches(TARGET2RE)) {
                    try {
                        // bullets in this email is · different from what google
                        // forms uses •
                        // MimeMultipart m = (MimeMultipart) msg.getContent();
                        String title = "Latest Community-eFliers";
                        eflier = new Blurb(title, getMultipartChild(msg), 1, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            // it's easier to remove the last element in resizing array
            if (eflier != null) blurbs.add(eflier);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // System.out.println("All Blurbs:");
        // for (Blurb blurb : blurbs) System.out.println(blurb.toString());

        return blurbs;
    }

    public static void testRead() {
        Properties props = new Properties();
        props.setProperty("mail.imap.ssl.enable", "true");
        Session session = Session.getInstance(props);

        // final String TARGET2 = "Fliers";// "eFliers to OM's";
        // final String TARGET2b = "Flyers";
        final String TARGET2RE = ".*(?i)fl[yi]ers.*";

        Store store;

        String password;
        try (Scanner sc = new Scanner(System.in)) {
            System.out.print(
                    "Password for office notes gmail: ");
            password = sc.nextLine();
        }
        // System.out.println("password is " + PASSWORD);

        try {
            store = session.getStore("imap");
            store.connect(HOST, USERNAME, password);

            Folder inbox = store.getFolder("INBOX");
            if (inbox == null) {
                System.out.println("Cannot get INBOX folder.");
                System.exit(1);
            }
            inbox.open(Folder.READ_ONLY);
            int totalMessageCounts = inbox.getMessageCount();

            if (totalMessageCounts == 0) {
                System.out.println("Empty inbox folder.");
                inbox.close(false);
                store.close();
                System.exit(-1);
            }

      /*
       * the eflier message has 2 bodyparts. if of multiplart:
       * multipart/ALTERNATIVE; boundary=
       * _000_af130904c8a343259126d8f56c808481EXMB01edmondswednetedu_
       */
            for (int i = 1; i < totalMessageCounts; i++) {
                Message msg = inbox.getMessage(i);
                String msgSubject = msg.getSubject();
                if (msgSubject.matches(TARGET2RE)) {
                    System.out.println(getMultipartChild(msg));
                }
            }

        } catch (NoSuchProviderException e1) {
            e1.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String getMultipartChild(Part p)
            throws MessagingException, IOException {
        final String EFLIERS = "You can find many activities such as Sports and"
                + " Arts lessons, Community Activities, and Youth Organizations"
                + ", etc., all of which are approved by the Edmonds School "
                + "District. You can view all Community eFliers at http://www"
                + ".edmonds.wednet.edu/Domain/130 on the District website under"
                + " the \"Community\" link on the homepage.";

        String result = EFLIERS;
        // System.out.println("getEmailChild called:");
        // System.out.println(p.getContentType());
        // System.out.println(p.getContent());

        MimeMultipart m = (MimeMultipart) p.getContent();
        int n = m.getCount();

        for (int j = 0; j < n; j++) {
            Part b = m.getBodyPart(j);
            // System.out.println("bodypart " + j + b.getContentType() + "000
            // ");
            if (b.isMimeType("text/plain")) {
                String fliers = (String) b.getContent();
                // System.out.println("flier found" + fliers);
                int listStart = 0;

                final String LIST_PATTERN = "(-\\s[\\w\\s&-:\"<0-9/]+(https?|ftp|file)"
                        + "://[-a-zA-Z0-9+&#/%?=~_|!:,.;>]"
                        + "+\\s*:\\s*[-a-zA-Z0-9;\\s+&#/:(\"%=~_|–,]+[)](\\sefliers)?)+";
                Pattern pattern = Pattern.compile(LIST_PATTERN);
                Matcher matcher = pattern.matcher(fliers);
                // int count = 0;
                if (matcher.find()) listStart = matcher.start();
                int listEnd = fliers.length();
                while (matcher.find()) {
                    // count++;
                    // System.out.println("Match number "
                    //     + count);
//                    System.out.println("start(): "
//                            + matcher.start());
//                    System.out.println("end(): "
//                            + matcher.end());
//                    System.out.println("matched segment: " + result.substring(matcher.start(), matcher.end()));
                    listEnd = matcher.end();
                }
                fliers = fliers.substring(listStart, listEnd);
//                System.out.println(result);
                fliers = fliers.replaceAll("((<br />)\\s*){2,}", "<br>")
                        .replaceAll("\\s{2,}", " ")
                        // .replace("<htt", ", eFlier at htt").replace(">", "")
                        .replaceAll("(<)(\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?="
                                + "~_|!:,\".;]*[-a-zA-Z0-9+&@#/%=~_|])(>)", ", eFlier at $2")
                        .replace("·", "<br />•").replace("- ", "<br />•")
                        .replace("&", "&amp;");
                // .replace(System.lineSeparator(), "")
                // .replaceAll("((<br />)\\s*){2,}", "<br>")
                result += fliers;
                break;
        /*
         * Without break same text appears twice in my tests.
         */
            } else if (b.isMimeType("text/html")) {
                String html = (String) b.getContent();
                result = result + "text/html " + j + "\n" + html;
                System.out.println(result);
            } else if (b.getContentType().contains("multipart")) {
                return getMultipartChild(b);
            }
        }

        return result;
    }

    public static void main(String[] args) {

        Scanner scanner1 = new Scanner(System.in);

        System.out.print("Password for Office Notes Gmail: ");
        String password = scanner1.nextLine();
        scanner1.close();

        List<Blurb> blurbs = ReadGmail.fetchBlurbs(password);
        Blurb.writeBlurbs("io/new-blurbs-test.html", blurbs);

        // System.out.println("mandu".indexOf("man", 0));

        // testRead();
        // System.out.println("FW : eflyers to
        // OM's".matches(".*(?i)fl[yi]ers.*"));
    }
}
