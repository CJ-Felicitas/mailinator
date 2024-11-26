package com.mailinator.mailer.controllers;

import com.apollomailgun.apollomailgun.services.MailService;
import com.apollomailgun.apollomailgun.services.PollService;
import com.apollomailgun.apollomailgun.services.UpdateEmailStatusService;
import com.fasterxml.jackson.core.io.JsonEOFException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.logging.Logger;
import java.util.Map;

@RequestMapping("/api")
@RestController
public class ApiController {

    private static final Logger logger = Logger.getLogger(ApiController.class.getName());

    @Autowired
    private UpdateEmailStatusService updateEmailStatusService;

    @Autowired
    private MailService mailService;

    @Autowired
    private PollService pollService;

    //  test for delivery status
    @PostMapping(path = "/deliveryStatus", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deliveryStatus(@RequestBody Map<String, Object> body) throws JsonEOFException {
        updateEmailStatusService.updateEmailStatus(body);
    }

   @PostMapping(path = "/sendmail", produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<Object> sendMail(@RequestBody Map<String, String> body) throws IOException {
       try {
           mailService.send(body.get("to"), body.get("subject"), body.get("message-id"), body.get("data"), Long.parseLong(body.get("template-id")), body.get("filepath"));
       } catch (IOException e) {
           throw new RuntimeException(e);
       }
       return ResponseEntity.ok(Map.of("status", "success", "message", "Email sent successfully"));
   }

}
