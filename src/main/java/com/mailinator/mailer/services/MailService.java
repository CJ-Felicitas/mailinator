package com.mailinator.mailer.services;

import com.apollomailgun.apollomailgun.configs.MailgunConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class MailService {

    @Autowired
    private MailgunConfig mailgunConfig;

    @Autowired
    private PollService pollService;

    @Autowired
    private ResourceService resourceService;

    private String getMailgunApiUrl() {
        return mailgunConfig.getBase_url() + mailgunConfig.getDomain() + "/messages";
    }

//  converters
    private static Map<String, Object> convertJsonToMap(String jsonString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
    }

//    send email method
    @Async
    public void send(String to, String subject, String messageId, String data /*json format*/, String filepath) throws IOException {

//      get the email template and pass the json data into the jinjava to render the html template
        Map<String, Object> context = convertJsonToMap(data);
        Long templateID = 1L;
        String renderedTemplate = resourceService.getEmailTemplate(context, templateID);

//      get the pdf file
        ClassPathResource pdf = resourceService.getPDF(filepath);
        log.info("file path is : " + filepath );
//      payload for the request
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("from", mailgunConfig.getFrom());
        body.add("to", to);
        body.add("subject", subject);
        body.add("html", renderedTemplate);
        body.add("o:tracking", "yes");

//      message id is added to track the status of message delivery in webhook of the mailgun
        body.add("v:email_id", messageId);
        body.add("attachment", pdf);

//      set the headers for the request
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("api", mailgunConfig.getApikey());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

//      create the request entity
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = getMailgunApiUrl();

//      send the request
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
    }


//  the method that will be called from the scheduler
    @Async
    public void sendEmails() throws Exception {
        
        pollService.poll().forEach(invoiceNotifications -> {
            try {
                this.send(invoiceNotifications.getTargetEmail(), "Invoice", invoiceNotifications.getId().toString(), invoiceNotifications.getJsonData(), invoiceNotifications.getFilepath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

} 
