package com.mailinator.mailer.services;

import com.apollomailgun.apollomailgun.configs.MailgunConfig;
import com.apollomailgun.apollomailgun.models.entities.InvoiceNotifications;
import com.apollomailgun.apollomailgun.models.repository.InvoiceNotificationRepository;
import com.fasterxml.jackson.core.io.JsonEOFException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Optional;

@Service
public class UpdateEmailStatusService {
    private static final Logger logger = Logger.getLogger(UpdateEmailStatusService.class.getName());
    @Autowired
    private InvoiceNotificationRepository invoiceNotification;

    @Autowired
    private MailgunConfig mailgunConfig;

    public void updateEmailStatus(Map<String, Object> body) throws JsonEOFException {
        try {

            String jsonString = new com.google.gson.Gson().toJson(body);
            JsonElement element = JsonParser.parseString(jsonString);
            JsonObject jsonObject = element.getAsJsonObject();
            JsonObject eventData = jsonObject.getAsJsonObject("event-data");
            String status = eventData.get("event").getAsString();
            JsonObject userVariables = eventData.getAsJsonObject("user-variables");
            Long id = userVariables.get("email_id").getAsLong();

            // Update the invoice status
            Optional<InvoiceNotifications> invoice = invoiceNotification.findById(id);

            if (invoice.isPresent()) {
                InvoiceNotifications invoiceNotifications = invoice.get();
                int attempts = invoiceNotifications.getAttempts();
//                check first if the email has already been delivered
                if ("delivered".equalsIgnoreCase(invoiceNotifications.getEmailed())) {
//                    for unexpected cases where the email is delivered more than once
                    logger.info("Status received");
                    logger.info("Email already delivered :: No status update needed");
                    logger.info("Email address: " + invoiceNotifications.getTargetEmail());
                }
                // check if the status is delivered and check the number of attempts before updating the status
                else if ("delivered".equalsIgnoreCase(status) && attempts < mailgunConfig.getMaxAttempts()) {
                    invoiceNotifications.setAttempts(invoiceNotifications.getAttempts() + 1);
                    logger.info("Status received");
                    logger.info("Email status updated: " + status);
                    logger.info("Email address: " + invoiceNotifications.getTargetEmail());
                    invoiceNotifications.setEmailed(status);
                    invoiceNotification.save(invoiceNotifications);
                }
//               check first if the email has already been at the state of permanent failure
                else if("retry".equalsIgnoreCase(invoiceNotifications.getEmailed()) && attempts >= mailgunConfig.getMaxAttempts()){
//                  if retry attempts reaches 5, set the status to failed which means permanent failure
                    String permanent_failure = "failed";
                    invoiceNotifications.setEmailed(permanent_failure);
                    invoiceNotification.save(invoiceNotifications);
                    logger.info("Status received");
                    logger.info("Email has reached max attempts | will not retry");
                }
//                check if the status is failed and check the number of attempts before updating the status
                else if ("failed".equalsIgnoreCase(status) && attempts < mailgunConfig.getMaxAttempts()) {
                    invoiceNotifications.setAttempts(invoiceNotifications.getAttempts() + 1);
                    logger.info("Status received");
                    logger.info("Email status updated: " + status+ " | will retry on next poll | retry attempts: " + attempts);
                    logger.info("Email address: " + invoiceNotifications.getTargetEmail());
                    String custom_status = "retry";
                    invoiceNotifications.setEmailed(custom_status);
                    invoiceNotification.save(invoiceNotifications);
                }
            } else {
                logger.info("Invoice not found :: No email status updated");
            }
        } catch (JsonSyntaxException e) {
            logger.severe("Failed to parse JSON: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Failed to update email status: " + e.getMessage());
        }
    }
} 