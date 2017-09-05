package zhuang.jesse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import zhuang.jesse.config.MailChimpConfig;
import zhuang.jesse.entity.Campaign;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MailChimpConfig.class)
public class MailChimpConfigTest {

    @Autowired
    private Campaign campaign;

    @Test
    public void assertCampaignProperties() {
        assertEquals("regular", campaign.getType());
    }

}
