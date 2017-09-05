package zhuang.jesse.mailchimp;

import com.ecwid.maleorang.MailchimpClient;
import com.ecwid.maleorang.MailchimpException;
import com.ecwid.maleorang.method.v3_0.campaigns.CampaignActionMethod;
import com.ecwid.maleorang.method.v3_0.campaigns.CampaignInfo;
import com.ecwid.maleorang.method.v3_0.campaigns.EditCampaignMethod;
import com.ecwid.maleorang.method.v3_0.campaigns.content.SetCampaignContentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import zhuang.jesse.config.AppConfig;
import zhuang.jesse.constants.MailChimpConstants;
import zhuang.jesse.util.FileUtils;
import zhuang.jesse.util.TimeUtils;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

@Component
public class EcwidCampaignFactory {

    private static final Logger LOGGER = Logger.getLogger(EcwidCampaignFactory.class.getName());

    private static final MailchimpClient CLIENT = new MailchimpClient(MailChimpConstants.API_KEY);

    @Autowired
    private Environment env;

    public String createCampaign() {

        EditCampaignMethod.Create job = new EditCampaignMethod.Create();
        job.type = CampaignInfo.Type.REGULAR;
        job.recipients = new CampaignInfo.RecipientsInfo();
        job.recipients.list_id = env.getProperty("list_id");

        job.settings = new CampaignInfo.SettingsInfo();
        job.settings.subject_line = MailChimpConstants.EMAIL_SUBJECT;
        job.settings.from_name = env.getProperty("from_name");
        job.settings.reply_to = env.getProperty("reply_to");
        job.settings.folder_id = env.getProperty("folder_id");
        job.settings.auto_tweet = Boolean.valueOf(env.getProperty("auto_tweet"));
        job.settings.title = MailChimpConstants.CAMPAIGN_TITLE;
        job.settings.to_name = MailChimpConstants.TO_NAME;

        job.social_card = new CampaignInfo.SocialCardInfo();
        job.social_card.image_url = env.getProperty("social_url");
        job.social_card.description = env.getProperty("social_description");
        job.social_card.title = MailChimpConstants.CAMPAIGN_TITLE;;
//        method.settings.template_id = Integer.valueOf(env.getProperty("template_id"));

        CampaignInfo campaignInfo;

        try {
            campaignInfo = CLIENT.execute(job);
        } catch (IOException | MailchimpException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create campaign.");
        }

        LOGGER.info("Created campaign with id " + campaignInfo.id);

        return campaignInfo.id;
    }

    public void setCampaignCotent(String campaignId) {

        SetCampaignContentMethod job = new SetCampaignContentMethod(campaignId);

        if (MailChimpConstants.LEFT_COLUMN == null) MailChimpConstants.LEFT_COLUMN =
                FileUtils.readFileToString("io/forMailchimpLeft.html");
        if (MailChimpConstants.RIGHT_COLUMN == null) MailChimpConstants.RIGHT_COLUMN =
                FileUtils.readFileToString("io/forMailchimpRight.html");

        job.html = MailChimpConstants.PART1 + MailChimpConstants.RIGHT_COLUMN + MailChimpConstants.PART2
                + MailChimpConstants.LEFT_COLUMN + MailChimpConstants.PART3;

        try {
            CLIENT.execute(job);
        } catch (IOException | MailchimpException e) {
            e.printStackTrace();
            throw new RuntimeException("Set campaign content failed for " + campaignId);
        }
    }

    public void scheduleCampaign(String campaignId) {

        CampaignActionMethod.Schedule job = new CampaignActionMethod.Schedule(campaignId);
        job.schedule_time = Date.from(TimeUtils.getComingMonday6am().toInstant());
        try {
            CLIENT.execute(job);
        } catch (IOException | MailchimpException e) {
            e.printStackTrace();
            throw new RuntimeException("Schedule campaign failed for " + campaignId);
        }

    }

    public void doAllCampaignJobs() {
        String campaignId = createCampaign();
        setCampaignCotent(campaignId);
        scheduleCampaign(campaignId);
    }

    public static void main(String[] args) {
        ApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        EcwidCampaignFactory cc = context.getBean(EcwidCampaignFactory.class);

//        cc.createCampaign();
//        cc.setCampaignCotent("df17d9e6b1");
//        cc.scheduleCampaign("df17d9e6b1");
        cc.doAllCampaignJobs();
    }
}
