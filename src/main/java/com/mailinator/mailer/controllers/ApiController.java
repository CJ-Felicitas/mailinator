package com.mailinator.mailer.controllers;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.mailinator.mailer.services.MailService;
import java.util.Map;

@RequestMapping("/api")
@RestController
public class ApiController {

    @Autowired
    private MailService mailService;

   @PostMapping(path = "/sendmail", produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<Object> sendMail(@RequestBody Map<String, String> body) throws IOException {
      return mailService.send(body);
   }

}
