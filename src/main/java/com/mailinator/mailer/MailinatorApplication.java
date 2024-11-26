package com.mailinator.mailer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class MailinatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MailinatorApplication.class, args);
	}

}
