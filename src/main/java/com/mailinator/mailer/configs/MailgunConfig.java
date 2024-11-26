package com.mailinator.mailer.configs;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class MailgunConfig {
    @Value("${mailgun.max_recepients}")
    private int maxRecepients;

    @Value("${mailgun.apikey}")
    private String apikey;

    @Value("${mailgun.domain}")
    private String domain;

    @Value("${mailgun.from}")
    private String from;

    @Value("${mailgun.base_url}")
    private String base_url;

    @Value("${mailgun.max_attempts}")
    private int maxAttempts;
}
