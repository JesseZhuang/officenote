package zhuang.jesse;


import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import zhuang.jesse.config.AppConfig;
import zhuang.jesse.entity.Blurb;
import zhuang.jesse.google.ReadGcal;
import zhuang.jesse.google.ReadGmail;
import zhuang.jesse.mailchimp.EcwidCampaignFactory;

import java.io.IOException;
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
    // if this app is run from command line folder path needs to be ../io/
    private static final String FOLDER = "io/";
    private static final String NEW_BLURB = FOLDER + "new-blurbs.html";
    private static final String STAYON_BLURB = FOLDER + "stay-on-blurbs.html";
    private static final String ARCHIVED_BLURB = FOLDER
            + "archived-blurbs.html";
    private static final String GOOGLE_DOC = FOLDER + "forGoogleDoc.txt";
    private static final String MAILCHIMP_LEFT = FOLDER
            + "forMailchimpLeft.html";
    private static final String MAILCHIMP_RIGHT = FOLDER
            + "forMailchimpRight.html";

    private static void fetchBlurbs(String saveToFile) {

        Scanner scanner1 = new Scanner(System.in);
        System.out.print(
                "Before we start, do you want to archive the blurbs from last week? ");
        if (scanner1.nextLine().toLowerCase().startsWith("y"))
            Blurb.updateBlurbs(NEW_BLURB, STAYON_BLURB, ARCHIVED_BLURB);
        System.out.print("Password for Office Notes Gmail: ");
        String password = scanner1.nextLine();
        scanner1.close();

        List<Blurb> blurbs = ReadGmail.fetchBlurbs(password);

        System.out.println("Finished reading office note submissions.");

        Blurb.writeBlurbs(saveToFile, blurbs);
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
    public static void main(String[] args) {

        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

        final String USAGE = "Usage: java -jar office-note.jar [-option]\n"
                + "available options:\n"
                + "  -fb\tto fetch blurbs from madronaofficenotes gmail account;\n"
                + "  -wf\tto write google doc file and two html files for mailchimp;\n"
                + "  -wfmc\tto write mailchimp files only;\n"
                + "  -mc\tto create mailchimp email campaign;\n";

        if (args.length != 1) System.out.println(USAGE);
        else {
            switch (args[0]) {
                case "-fb":
                    fetchBlurbs(NEW_BLURB);
                    break;
                case "-wf":
                    try {
                        ReadGcal.writeGcalEvents(GOOGLE_DOC, MAILCHIMP_LEFT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    List<Blurb> blurbs = addBlurbs(NEW_BLURB, STAYON_BLURB);
                    blurbs = Blurb.writeBlurbsForMailchimp(MAILCHIMP_RIGHT, blurbs);
                    Blurb.writeBlurbsForGoogleDoc(GOOGLE_DOC, blurbs);
                    System.out.println("Finished generating 2 MailChimp and 1 GoogleDoc files.");
                    break;
                case "-wfmc":
                    List<Blurb> blurbs2 = addBlurbs(NEW_BLURB, STAYON_BLURB);
                    Blurb.writeBlurbsForMailchimp(MAILCHIMP_RIGHT, blurbs2);
                    break;
                case "-mc":
                    EcwidCampaignFactory campaignFactory = applicationContext.getBean(EcwidCampaignFactory.class);
                    campaignFactory.doAllCampaignJobs();
                default:
                    System.out.println(USAGE);
                    break;
            }
        }

        // updateBlurbs(NEW_BLURB, "io/stay-on-blurbs-test.html",
        // "io/archived-blurbs-test.html");
    }
}
