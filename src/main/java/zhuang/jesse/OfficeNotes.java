package zhuang.jesse;


import org.apache.commons.cli.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import zhuang.jesse.config.AppConfig;
import zhuang.jesse.constants.MailChimpConstants;
import zhuang.jesse.eflier.EflierCrawler;
import zhuang.jesse.entity.Blurb;
import zhuang.jesse.entity.Eflier;
import zhuang.jesse.google.GoogleDoc;
import zhuang.jesse.google.ReadGcal;
import zhuang.jesse.google.ReadGmail;
import zhuang.jesse.mailchimp.EcwidCampaignFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This is the Main application for the 2nd generation Java office notes
 * automatic production app. New features for the update:
 * <p>
 * version 2.2 update (2/26/2016)
 * <ul>
 * <li>process the e-flier email with JavaMail API;
 * <li>convert list items to proper format in HtmlConverter with regular
 * expression.
 * </ul>
 * Version 2.1 features(done 02/08/16):
 * <ol>
 * <li>implement google calendar API to pull events from bearfacts calendar.
 * <li>identify possible list bullets and replace those to <i>ul</i> and
 * </ol>
 * Version 2.0 updates:
 * <ol>
 * <li>using javamail API to obtain office notes submission emails.
 * <li>saving blurbs to html file.
 * <li>reading blurbs from the saved html file.
 * <li>using css to control fonts in the html files for mailchimp.
 * </ol>
 *
 * @author Zexi Jesse Zhuang
 */

public class OfficeNotes {
    private static ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

    // if this app is run from command line folder path needs to be ../io/
    private static final String FOLDER = "io/";
    private static final String NEW_BLURB = FOLDER + "new-blurbs.html";
    private static final String STAYON_BLURB = FOLDER + "stay-on-blurbs.html";
    private static final String ARCHIVED_BLURB = FOLDER
            + "archived-blurbs.html";
    private static final String GOOGLE_DOC = FOLDER + "forGoogleDoc.txt";
    private static final String MAILCHIMP_LEFT = FOLDER + "forMailchimpLeft.html";
    private static final String MAILCHIMP_RIGHT = FOLDER + "forMailchimpRight.html";
    private static final String MAILCHIMP_WHOLE = FOLDER + "forMailchimp.html";

    private static void fetchBlurbs(String saveToFile, boolean doArchive, String password) {

        List<Blurb> blurbs = ReadGmail.fetchBlurbs(password);
        System.out.println("Read gmail job finished at " + LocalDateTime.now() +
                " \nFinished reading office note submissions.");

        blurbs.addAll(buildEflierBourbWithCrawler());
        System.out.println("Finished crawling efliers.");
        if (blurbs.size() == 0) {
            System.out.println("No submissions this week.\n");
            System.exit(1);
        }

        if (doArchive) {
            Blurb.updateBlurbs(NEW_BLURB, STAYON_BLURB, ARCHIVED_BLURB);
            System.out.println("Updated and archived expired blurbs.");
        }
        Blurb.writeBlurbs(saveToFile, blurbs);
    }

    private static List<Blurb> buildEflierBourbWithCrawler() {
        List<Blurb> eflierBlurb = new ArrayList<>();
        final String title = "Latest Community-eFliers";
        EflierCrawler crawler = new EflierCrawler();
        List<Eflier> efliers = crawler.crawlAllEfliers();
        String content = "The following efliers are obtained by Jesse Zhuang from " +
                "http://www.edmonds.wednet.edu/community/community_e_fliers and error can be caused by irregular " +
                "format of the listed efliers. In case of error, you should help to contact Oscar " +
                "at halperto@edmonds.wednet.edu or (425)431-7045 and Edmonds School District " +
                "to provide feedback that the format should be " +
                "kept with a standard.";
        if (!efliers.isEmpty()) {
            for (Eflier flier : efliers) content += flier;
            eflierBlurb.add(new Blurb(title, content, 1, 1));
        }
        return eflierBlurb;
    }

    private static List<Blurb> addBlurbs(String newBlurbFile,
                                         String stayOnBlurbFile) {
        List<Blurb> newBlurbs;
        List<Blurb> stayOnBlurbs;

        newBlurbs = Blurb.readBlurbs(newBlurbFile);
        Blurb eflier = null;
        int last = newBlurbs.size() - 1;
        if (newBlurbs.get(last).getTitle().equals("Latest Community eFliers"))
            eflier = newBlurbs.remove(last);
        stayOnBlurbs = Blurb.readBlurbs(stayOnBlurbFile);
        newBlurbs.addAll(stayOnBlurbs);
        if (eflier != null) newBlurbs.add(eflier);
        return newBlurbs;
    }

    private static void wholeJob(boolean doArchive, String password) throws IOException {
        fetchBlurbs(NEW_BLURB, doArchive, password);
        writeFiles();
        mailchimpJob();
        googleDocJob();
        System.out.println("Finished whole job.");
    }

    private static void mailchimpJob() {
        EcwidCampaignFactory campaignFactory = applicationContext.getBean(EcwidCampaignFactory.class);
        campaignFactory.doAllCampaignJobs();
        System.out.println("Finished mailchimp job.");
    }

    private static void googleDocJob() throws IOException {
        GoogleDoc googleDoc = applicationContext.getBean(GoogleDoc.class);
        googleDoc.wholeJob();
        System.out.println("Finished google doc job.\n");
    }

    private static void writeFiles() throws IOException {
        try {
            ReadGcal.writeGcalEvents(GOOGLE_DOC, MAILCHIMP_LEFT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Blurb> blurbs = addBlurbs(NEW_BLURB, STAYON_BLURB);
        blurbs = Blurb.writeBlurbsForMailchimp(MAILCHIMP_RIGHT, blurbs);
        Blurb.writeBlurbsForGoogleDoc(GOOGLE_DOC, blurbs);
        MailChimpConstants.mailchimpWhole = MailChimpConstants.PART1 + MailChimpConstants.mailchimpRightColumn
                + MailChimpConstants.PART2 + MailChimpConstants.mailchimpLeftColumn + MailChimpConstants.PART3;
        Files.write(Paths.get(MAILCHIMP_WHOLE), MailChimpConstants.mailchimpWhole.getBytes());
        System.out.println("Finished generating 3 MailChimp and 1 GoogleDoc files.");
    }

    /**
     * For now, three steps:
     * <p>
     * 1 fetch the blurbs and save it to a file (new-blurbs.html): option -fb.
     * <p>
     * 2 Manually examine the new-blurbs.html file (may need to add in <i>ul</i>
     * and <i>li</i> tags for lists, I may implement an automatic replacement
     * soon). Also need to pick 3 community e-fliers to add in (this is a fun
     * part which I do not want to automate).
     * <p>
     * 3 Read the blurbs from the new-blurbs.html file and the stay-on blurbs
     * file stay-on-blurbs.html and write pure text file forGoogleDoc.txt for
     * google doc MIT chair review: option -wgd.
     * <p>
     * 4 After MIT chair reviews the google doc, manually update the changes to
     * the blurb file.
     * <p>
     * 5 Read blurbs from new-blurbs.html and stay-on-blurbs.html and write two
     * html files for the mailchimp left (calendar and sticky news)
     * forMailChimpLeft.html and right (main contents: blurbs)
     * forMailChimpRight.html columns: option -wmc.
     * <p>
     * 6 After the office notes is published and sent (currently the schedule is
     * every Monday), read blurbs from the two blurb files (new and stay-on) and
     * update each blurb (update current week field, e.g., from (1 of 2 weeks)
     * to (2 of 2 weeks)). Write stay-on-blurbs.html and <strong>append</strong>
     * archived-blurbs.html. Option: -u.
     *
     * @param args command line flags to specify specific tasks.
     */
    public static void main(String[] args) throws IOException {

        Options options = new Options();
        Option pwdOpt = new Option("p", "password", true, "password for office notes gmail account");
        pwdOpt.setRequired(true);
        options.addOption("a", "archive", false, "whether to archive last week's blurbs")
                .addOption(pwdOpt);

        OptionGroup jobsToDo = new OptionGroup();
        jobsToDo.setRequired(true);
        jobsToDo.addOption(new Option("all", "all-jobs", false, "do all office note jobs"))
                .addOption(new Option("fb", "fetch-blurbs", false, "fetch blurbs from office " +
                        "notes gmail"))
                .addOption(new Option("wf", "write-files", false, "write files for mailchimp " +
                        "and google doc"))
                .addOption(new Option("mc", "mailchimp", false, "create and schedule mailchimp " +
                        "email campaign"))
                .addOption(new Option("gd", "google-doc", false, "upload google doc, " +
                        "share for review"));
        options.addOptionGroup(jobsToDo);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        boolean doArchive = false;
        String password;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("a")) doArchive = true;
            password = cmd.getOptionValue("password");
            if (cmd.hasOption("all")) wholeJob(doArchive, password);
            if (cmd.hasOption("fb")) fetchBlurbs(NEW_BLURB, doArchive, password);
            if (cmd.hasOption("wf")) writeFiles();
            if (cmd.hasOption("mc")) mailchimpJob();
            if (cmd.hasOption("gd")) googleDocJob();

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Office Note Java Application", options);

            System.exit(1);
            return;
        }
    }
}
