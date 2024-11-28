package com.mailinator.mailer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class MailinatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MailinatorApplication.class, args);
	}

}
