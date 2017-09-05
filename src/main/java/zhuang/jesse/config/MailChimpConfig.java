package zhuang.jesse.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import zhuang.jesse.constants.MailChimpConstants;
import zhuang.jesse.entity.Campaign;

/**
 * MailChimp beans config.
 */
@Configuration
@PropertySource("classpath:mailchimp.properties")
public class MailChimpConfig {

    @Autowired
    private Environment env;

    @Bean
    public Campaign campaign () {
        return new Campaign(MailChimpConstants.CAMPAIGN_TYPE);
    }

    @Bean
    public RestTemplate restTemplateBasicAuth () {
        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(MailChimpConstants.USERNAME,
//                MailChimpConstants.API_KEY));
        return restTemplate;
    }

}
