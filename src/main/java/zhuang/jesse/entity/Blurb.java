package zhuang.jesse.entity;


import zhuang.jesse.constants.MailChimpConstants;
import zhuang.jesse.util.FileUtils;
import zhuang.jesse.util.HtmlConverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The class represents a piece of news blurb to be included in an elementary
 * school newsletter.
 * <p>
 * Each week parent volunteers and teachers submit the news blurbs through a
 * google form. The blurbs are set up to arrive in simple HTML format through
 * email. Each submission specifies the number of weeks that the blurb should
 * included in the newsletter.
 * <p>
 * Use static method {@link #writeBlurbsForGoogleDoc(String, List)
 * writeBlurbsForGoogleDoc} method to generate a simple text file containing all
 * news blurbs for school parent committee chair to proofread and edit.
 */
public class Blurb {
    protected String title;
    private String content;
    private int numWeeks;
    /**
     * starting from 1, increment each week the blurb is included in the
     * newsletter.
     */
    private int curWeek;
    private String flyerLink;
    private String flyerURLs;
    private String imageURL;

    public String getTitle() {
        return title;
    }

    public Blurb(String title, String content, int curWeek, int howManyWeeks) {
        this.title = title;
        this.content = content;
        this.numWeeks = howManyWeeks;
        flyerLink = null;
        this.curWeek = curWeek;
    }

    public Blurb(String title, String content, int curWeek, int howManyWeeks,
                 String flyerLink) {
        this(title, content, curWeek, howManyWeeks);
        this.flyerLink = flyerLink;
    }

    /**
     * Sometimes there might be duplicate blurb submissions. Implement
     * {@code hashcode()} and {@code equals()} for deduplication.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result + numWeeks;
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Blurb other = (Blurb) obj;
        if (content == null) {
            if (other.content != null) return false;
        } else if (!content.equals(other.content)) return false;
        if (numWeeks != other.numWeeks) return false;
        if (title == null) {
            if (other.title != null) return false;
        } else if (!title.equals(other.title)) return false;
        return true;
    }

    /**
     * Used to save the blurbs in html format for editing.
     *
     * @return a customized String representation of the blurb
     */
    @Override
    public String toString() {
        String res = "Blurb title= " + title + "\nhowManyWeeks= (" + curWeek
                + " of " + numWeeks + " Weeks)" + "\ncontent= " + content;
        if (flyerLink != null) res += "\nflyer link= " + flyerLink;
        return res + "\n";
    }

    /**
     * Used to save the blurbs in simple text format for editing.
     *
     * @return a String representation of the blurb in simple text format with all
     * html tags removed.
     */
    public String toPureText() {
        String content = this.content.replace("<br>", System.lineSeparator());
        content = removeHtml(content);
        String title = this.title;
        title = removeHtml(title);
        String res = "Blurb title= " + title + "\nhowManyWeeks= (" + curWeek
                + " of " + numWeeks + " Weeks)" + "\ncontent=\n" + content;
        if (flyerLink != null) res += "\nflyer link= " + this.flyerURLs;
        return res + "\n\n";
    }

    private String removeHtml(String str) {
        return str.replace("<br />", System.lineSeparator()).replace("&amp;", "&")
                .replace("&#039;", "'").replace("&quot;", "\"")
                .replaceAll("<(\"[^\"]*\"|'[^']*'|[^'\">])*>", "");
    }

    private void update() {
        curWeek++;
    }

    /**
     * Each week before fetching new blurb submissions, all the blurbs in last
     * week's newsletter are updated with this method. Expired blurbs are archived
     * and not expired blurbs are written to the {@code stayOnFile}.
     *
     * @param newBlurbFile file containing this week's newly submitted blurbs
     * @param stayOnFile   file containing previous weeks staying-on blurbs
     * @param archiveFile  file for archiving expired blurbs
     */
    public static void updateBlurbs(String newBlurbFile, String stayOnFile,
                                    String archiveFile) {
        List<Blurb> blurbs = readBlurbs(stayOnFile);
        blurbs.addAll(readBlurbs(newBlurbFile));

        // update the blurbs using lambda expression
        blurbs.stream().forEach(b -> b.update());
        List<Blurb> archivedBlurbs = blurbs.stream()
                .filter(b -> b.curWeek > b.numWeeks).collect(Collectors.toList());

        blurbs.removeAll(archivedBlurbs);

        writeBlurbs(stayOnFile, blurbs);
        appendBlurbs(archiveFile, archivedBlurbs);
        System.out.println("Finished archiving last week's blurbs.");
    }

    /**
     * Save a {@code List} of Blurbs to a file.
     *
     * @param newBlurbFile file to save the blurbs to
     * @param blurbs       a {@code List} of Blurbs to be saved
     */
    public static void writeBlurbs(String newBlurbFile, List<Blurb> blurbs) {
        // FileOutputStream can be set to true for appending
        saveBlurbs(newBlurbFile, blurbs, false);
    }

    /*
     * can also use Files.newBufferedWriter(Paths.get(file)) by default uses
     * UTF-8, can specify StandardCharsets.UTF_8, also can specify open option.
     * see below write google doc one for reference. buffered writer uses
     * character streams 2 bytes, output stream writer encodes characters into
     * bytes, so 1 byte. Both are buffered.
     */
    private static void saveBlurbs(String saveToFile, List<Blurb> blurbs,
                                   boolean append) {
        // FileOutputStream can be set to true for appending
        File file = new File(saveToFile);
        if (!file.exists()) try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e1) {
            System.out.println("Cannot creat new file " + saveToFile);
            e1.printStackTrace();
        }
        try (OutputStreamWriter wr = new OutputStreamWriter(
                new FileOutputStream(saveToFile, append), "UTF-8")) {
            if (append)
                wr.write("Archived on " + Calendar.getInstance().getTime() + "\n");
            // wr.write("Archived on " + LocalDateTime.now() + "\n");
            for (Blurb blurb : blurbs) {
                wr.write(blurb.toString());
                wr.write("\n");
            }
            wr.close();

        } catch (Exception e) {
            System.out.println("Write file error.");
            e.printStackTrace();
        }
    }

    /**
     * Append a {@code List} of expired Blurbs to a archive file.
     *
     * @param saveToFile file to archive the blurbs to
     * @param blurbs     a {@code List} of Blurbs to be archived
     */
    public static void appendBlurbs(String saveToFile, List<Blurb> blurbs) {
        // FileOutputStream can be set to true for appending
        saveBlurbs(saveToFile, blurbs, true);
    }

    /**
     * Read previously saved Blurbs from a file.
     *
     * @param inputFile file containing the saved Blurbs
     * @return a {@code List} of Blurbs previously saved
     */
    public static List<Blurb> readBlurbs(String inputFile) {
        List<Blurb> blurbs = new ArrayList<Blurb>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(inputFile),
                StandardCharsets.UTF_8);) {
            String line = br.readLine();
            final String HEADER1 = "Blurb title= ";
            final String HEADER2 = "howManyWeeks= (";
            final String HEADER3 = " of ";
            final String HEADER4 = "content= ";
            final String HEADER5 = "flyer link= ";

            while (line != null && line.contains(HEADER1)) {
                // get title
                String title = line.substring(line.indexOf(HEADER1) + HEADER1.length());

                // get curWeeks
                line = br.readLine();
                int start = line.indexOf(HEADER2) + HEADER2.length();
                int curWeeks = Integer.parseInt(line.substring(start, start + 1));

                // get howManyWeeks
                start = line.indexOf(HEADER3) + HEADER3.length();
                int howManyWeeks = Integer.parseInt(line.substring(start, start + 1));

                // get content
                line = br.readLine();
                String content = line
                        .substring(line.indexOf(HEADER4) + HEADER4.length());

                // get flier link if available
                line = br.readLine();
                if (line != null && line.contains(HEADER5)) {
                    String flyerLink = line
                            .substring(line.indexOf(HEADER5) + HEADER5.length());
                    Blurb b = new Blurb(title, content, curWeeks, howManyWeeks,
                            flyerLink);
                    b.flyerURLs = getFlyerURLs(b);
                    blurbs.add(b);
                } else blurbs.add(new Blurb(title, content, curWeeks, howManyWeeks));
                // skip blank lines if any
                while ((line = br.readLine()) != null && !line.contains(HEADER1)) ;
            }

        } catch (Exception e) {
            System.out.println("Read file error.");
            e.printStackTrace();
        }
        return blurbs;
    }

    /**
     * This method generates all the html code to include all the blurbs in the
     * mailchimp newsletter.
     * <p>
     * A content list with anchor links pointing to the actual blurb is generated
     * on top. CSS codes are included to format the blurb title and content with
     * different fonts. Web url links, flyer links, and email addresses are
     * converted to appropriate formatted html code with another class
     * {@code HtmlConverter} in the same package.
     * <p>
     * If a picture flyer is available, it is included on the bottom of the
     * corresponding blurb.
     *
     * @param outFile html file generated for mailchimp newsletter
     * @param blurbs  a {@code List} of Blurbs including newly submitted and stay-on
     *                blurbs
     */
    public static List<Blurb> writeBlurbsForMailchimp(String outFile,
                                                      List<Blurb> blurbs) {

        List<Blurb> thisWeek = new ArrayList<Blurb>(),
                lastWeekOnNotes = new ArrayList<>(), pastWeeks = new ArrayList<>(),
                community = new ArrayList<>();

        for (Blurb blurb : blurbs) {
            if (blurb.title.contains("Community-")) community.add(blurb);
            else if (blurb.curWeek == 1) thisWeek.add(blurb);
            else if (blurb.curWeek == blurb.numWeeks) lastWeekOnNotes.add(blurb);
            else pastWeeks.add(blurb);
        }

        blurbs = new ArrayList<>();

        blurbs.addAll(thisWeek);
        blurbs.addAll(lastWeekOnNotes);
        blurbs.addAll(pastWeeks);
        blurbs.addAll(community);

        final String HEADER = "<!-- forMailChimpRight start -->\n" +
                "<style>\n.heading {color: rgb(128,0,0);font"
                + "-family: verdana,geneva,sans-serif; font-weight:bold;}\n"
                + ".date{font-family: verdana,geneva,sans-serif; font-weight:bold;}\n"
                + ".content{font-family:trebuchet ms,lucida grande,lucida sans"
                + " unicode,lucida sans,tahoma,sans-serif;}\nul{font-"
                + "family:trebuchet ms,lucida grande,lucida sans"
                + " unicode,lucida sans,tahoma,sans-serif;}\n"
                + "</style>\n<span class=\"heading\">New this week</span>\n<ul>";
        final String LIST_PRE1 = "<li class=\"content\"><a href=\"#";
        final String LIST_PRE2 = "\" target=\"_self\">";
        final String LIST_END = "</a></li>";
        final String HORIRULE = "\n<hr /><br>\n\n";

        try (BufferedWriter wr = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"))) {
            // generate the content list
            wr.write(HEADER);

            int counter = 0;
            for (Blurb blurb : thisWeek) {
                wr.write(LIST_PRE1 + counter + LIST_PRE2 + blurb.title + LIST_END);
                wr.newLine();
                counter++;
            }

            wr.write("</ul>\n\n<span class=\"heading\">Last time appearing "
                    + "in Notes</span>\n<ul>");

            for (Blurb blurb : lastWeekOnNotes) {
                wr.write(LIST_PRE1 + counter + LIST_PRE2 + blurb.title + LIST_END);
                wr.newLine();
                counter++;
            }

            wr.write("</ul>\n\n<span class=\"heading\">Past weeks</span>\n<ul>");

            for (Blurb blurb : pastWeeks) {
                wr.write(LIST_PRE1 + counter + LIST_PRE2 + blurb.title + LIST_END);
                wr.newLine();
                counter++;
            }

            wr.write("</ul>\n\n<span class=\"heading\">Community</span>\n<ul>");

            for (Blurb blurb : community) {
                wr.write(LIST_PRE1 + counter + LIST_PRE2 + blurb.title + LIST_END);
                wr.newLine();
                counter++;
            }

            wr.write("</ul>\n\n");

            wr.write(HORIRULE);

            // generate html for the main body

            // title tags
            String tPre1 = "<a id=\"";
            String tPre2 = "\" name=\"";
            String tPre3 = "\" style=\"text-decoration:none\"><span class="
                    + "\"heading\">";
            String tEnd = "</span></a>\n";
            // content tags
            String cPre = "<p class=\"content\">";
            String cEnd = "</p>\n";

            counter = 0;

            for (Blurb blurb : blurbs) {
                String flyerLink = blurb.flyerLink;
                String content = HtmlConverter.convert(blurb.content);
                String tPre = tPre1 + counter + tPre2 + counter + tPre3;
                wr.write(tPre + blurb.title + tEnd);
                if (flyerLink == null) wr.write(cPre + content + cEnd);
                else
                    wr.write(cPre + content + " Flyer is at " + flyerLink + "." + cEnd);

                // supports one picture flyer
                if (blurb.imageURL != null) {
                    String imgURL = blurb.imageURL;
                    String pre = "<a href='" + imgURL
                            + "' target='_blank' title='Click to view'>";
                    String end = "</a>";
                    wr.write("\n\n" + pre);
                    wr.write("<img width =\"300\"; src=\"" + imgURL + "\">" + end
                            + "\n<br><br>");
                }
                wr.newLine();
                counter++;
            }
            wr.write("<br><br>\n" + "<!-- forMailChimpRight end -->");

        } catch (IOException e) {
            System.out.println("Unable to write to file " + outFile.toString());
        }

        MailChimpConstants.mailchimpRightColumn = FileUtils.readFileToString(outFile);

        return blurbs;
    }

    private static String getFlyerURLs(Blurb blurb) {
        String flyerLink = blurb.flyerLink;
        String flyerURLs = "";
        int ind = -1, counter = 0;
        while ((ind = flyerLink.indexOf("http", ind + 1)) > 0) {
            String flyerURL = flyerLink.substring(ind, flyerLink.indexOf("'", ind));
            if (counter > 0) flyerURLs += " and ";
            // String flyerURLLC = flyerURL.toLowerCase();
            // only care about the first image URL
            if (blurb.imageURL == null
                    && flyerURL.matches("([^\\s]+(\\.(?i)(jpg|png|gif|bmp)).*)")) {
                blurb.imageURL = flyerURL.replace("&amp;", "&");
            }
            flyerURLs += flyerURL;
            counter++;
        }
        return flyerURLs;
    }

    /**
     * This method calls {@link #toPureText()} method to save all the blurbs in a
     * simple text format for parent chair to proof read and edit.
     *
     * @param outFile simple text file generated for proofreading and editing
     * @param blurbs  a {@code List} of Blurbs including newly submitted and stay-on
     *                blurbs
     */
    public static void writeBlurbsForGoogleDoc(String outFile,
                                               List<Blurb> blurbs) {
        // set write mode to append because blurbs written after the events
        try (BufferedWriter wr = Files.newBufferedWriter(Paths.get(outFile),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            for (Blurb blurb : blurbs) {
                wr.write(blurb.toPureText());
                wr.write("\n");
            }
            wr.close();

        } catch (Exception e) {
            System.out.println("Write file error.");
            e.printStackTrace();
        }

        MailChimpConstants.googleDoc = FileUtils.readFileToString(outFile);
    }

    public static void main(String[] args) {
        // use "..\\io\\blurbs-lastweek.html" if run from command line
        // List<Blurb> blurbs = Blurb.readBlurbs("io/new-blurbs-test.html");
        List<Blurb> blurbs = Blurb.readBlurbs("io/new-blurbs.html");

        // for (Blurb blurb : blurbs) {
        // System.out.println(blurb);
        // }

        Blurb.writeBlurbsForMailchimp("io/forMailchimp-test.html", blurbs);
        Blurb.writeBlurbsForGoogleDoc("io/forGoogleDoc-test.txt", blurbs);

    }
}