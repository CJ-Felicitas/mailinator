package com.mailinator.mailer.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

import com.mailinator.mailer.configs.MailgunConfig;

@Slf4j
@Service
public class MailService {

    @Autowired
    private MailgunConfig mailgunConfig;

    @Async
    public ResponseEntity<Object> send(Map<String, String> body) {

        MultiValueMap<String, Object> payload = new LinkedMultiValueMap<>();

        payload.add("from", body.get("from"));
        payload.add("to", body.get("to"));
        payload.add("subject", body.get("subject"));
        payload.add("o:tracking", "yes");

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("api", mailgunConfig.getApikey());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
        String url = mailgunConfig.getBase_url() + mailgunConfig.getDomain() + "/messages";

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return new ResponseEntity<>(response.getBody(), response.getStatusCode());
        } catch (Exception e) {
            log.error("Error sending email to {}: {}", body.get("to"), e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 
