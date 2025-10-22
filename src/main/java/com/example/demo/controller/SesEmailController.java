package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/emails")
public class SesEmailController {

    private SesClient sesClient;

    @Value("${aws.ses.sender}")
    private String senderEmail;

    public SesEmailController(final SesClient sesClient) {
        this.sesClient = sesClient;
    }


    @GetMapping("/identities")
    public List<String> listSESIdentities() {
        try {
            ListIdentitiesResponse identitiesResponse = sesClient.listIdentities();
            List<String> identities = identitiesResponse.identities();
            for (String identity : identities) {
                System.out.println("The identity is " + identity);
            }

            return identities;
        } catch (SesException e) {
            System.err.println(e.awsErrorDetails()
                                .errorMessage());
            System.exit(1);
        }
        return Collections.emptyList();
    }

    @PostMapping("/send")
    public String sendEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body
    ) {
        try {
            // Create the destination (who receives the email)
            Destination destination = Destination.builder()
                                                 .toAddresses(to)
                                                 .build();

            // Create the email content
            Content content = Content.builder()
                                     .data(body)
                                     .build();

            Content subj = Content.builder()
                                  .data(subject)
                                  .build();

            Body emailBody = Body.builder()
                                 .text(content)
                                 .build();

            Message message = Message.builder()
                                     .subject(subj)
                                     .body(emailBody)
                                     .build();

            // Build the email request
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                                                            .source(senderEmail) // Must be verified in AWS SES
                                                            .destination(destination)
                                                            .message(message)
                                                            .build();

            // Send the email
            sesClient.sendEmail(emailRequest);

            return "✅ Email sent successfully to " + to;
        } catch (SesException e) {
            e.printStackTrace();
            return "❌ Failed to send email: " + e.awsErrorDetails()
                                                 .errorMessage();
        }
    }
}
