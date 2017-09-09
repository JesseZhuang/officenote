package zhuang.jesse.mailchimp;


import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import zhuang.jesse.config.AppConfig;
import zhuang.jesse.constants.MailChimpConstants;
import zhuang.jesse.entity.Campaign;
import zhuang.jesse.util.TimeUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

@Component
public class CampaignFactory {

    @Autowired
    private Environment env;

    @Autowired
    private Campaign campaign;

    @Autowired
    private RestTemplate restTemplate;

    private static final Logger LOGGER = Logger.getLogger(CampaignFactory.class.getName());

    public Campaign createCampaign() throws IOException {

        // test client
//        LOGGER.info(restTemplate.getForEntity(MailChimpConstants.API_HOME, String.class).getBody());

        HashMap<String, Object> params = new LinkedHashMap<>();

        params.put("type", MailChimpConstants.CAMPAIGN_TYPE);

        HashMap<String, String> recipients = new HashMap<>();
        recipients.put("list_id", env.getProperty("mail.list.id"));
        params.put("recipients", recipients);

        HashMap<String, String> settings = new HashMap<>();
        settings.put("subject_line", MailChimpConstants.EMAIL_SUBJECT);
        settings.put("title", MailChimpConstants.CAMPAIGN_TITLE);
        settings.put("from_name", env.getProperty("from_name"));
        settings.put("reply_to", env.getProperty("reply_to"));
        settings.put("to_name", MailChimpConstants.TO_NAME);
        settings.put("folder_id", env.getProperty("folder_id"));
        settings.put("auto_tweet", env.getProperty("auto_tweet"));
        params.put("settings", settings);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.ALL));
        System.out.println(MailChimpConstants.API_KEY);
        String encoding = Base64.getEncoder().encodeToString((MailChimpConstants.USERNAME + ":"
                + MailChimpConstants.API_KEY).getBytes(StandardCharsets.UTF_8));
        System.out.println(encoding);
        headers.add("Authorization", "Basic " + encoding);

//        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
//        Map map = new HashMap<String, String>();
//        map.put("content-type", "application/json");
//
//        headers.setAll(map);

//        String paramStr = getJSOnStr(params);


        HttpEntity<HashMap<String, Object>> request = new HttpEntity<>(params, headers);
//        HttpEntity<HashMap<String, Object>> request = new HttpEntity<>(params, headers);

//        Campaign campaign = restTemplate.postForObject(MailChimpConstants.API_HOME + "campaigns",
//                request, Campaign.class);

        List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
        converters.add(new MappingJackson2HttpMessageConverter());

        restTemplate.setMessageConverters(converters);

//        HttpEntity<?> getRequest = new HttpEntity<>(new HashMap<String, Object>(), headers);
//
//        ResponseEntity<String> responseEntity = restTemplate.exchange(MailChimpConstants.API_HOME, HttpMethod.GET,
//                getRequest, String.class);
//
//        LOGGER.info(responseEntity.getBody());

//        ResponseEntity<String> response = restTemplate.exchange(MailChimpConstants.API_HOME + "campaigns",
//                HttpMethod.POST, request, String.class);
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:8080", HttpMethod.POST,
                request, String.class);

        LOGGER.info("Created campaign" + response.getStatusCode() + response.getHeaders());

        return campaign;
    }

    public String getJSOnStr(Object obj) throws JsonGenerationException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        Writer strWriter = new StringWriter();
        mapper.writeValue(strWriter, obj);
        String userDataJSON = strWriter.toString();
//        System.out.println(URLEncoder.encode(userDataJSON));
//        System.out.println(userDataJSON);
        return userDataJSON;

    }

    public static void main(String[] args) throws IOException {

//        ApplicationContext context =
//                new AnnotationConfigApplicationContext(AppConfig.class);
//
//        CampaignFactory cc = context.getBean(CampaignFactory.class);
//        Normally Spring cannot wire into external https://stackoverflow.com/questions/310271
//        CampaignFactory cc = new CampaignFactory();

//        ResponseEntity<Campaign>  response = restTemplate.postForEntity(MailChimpConstants.API_HOME,
//                campaign, Campaign.class);

//        ResponseEntity<String> response2 = cc.restTemplate.getForEntity(MailChimpConstants.API_HOME, String.class);
//        ResponseEntity<String> response2 = cc.restTemplate.getForEntity("http://localhost:8080", String.class);
//
//        System.out.println(response2.getStatusCode());
//        System.out.println(response2.getBody());

//        System.out.println(cc.campaign.getType());

//        cc.createCampaign();
        System.out.println(LocalDateTime.now());
    }
}
