package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class SqsMessageController {

    private final SqsClient sqsClient;

    @Value("${aws.sqs.queue}")
    private String queueName;

    public SqsMessageController(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    @GetMapping("/receive")
    public List<String> receive() {
        String queueUrl = sqsClient.getQueueUrl(b -> b.queueName(queueName)).queueUrl();
        return sqsClient.receiveMessage(r -> r.queueUrl(queueUrl).maxNumberOfMessages(10))
                        .messages().stream().map(m -> m.body()).toList();
    }

    @PostMapping("/send")
    public String send(@RequestParam String message) {
        String queueUrl = sqsClient.getQueueUrl(b -> b.queueName(queueName)).queueUrl();
        sqsClient.sendMessage(r -> r.queueUrl(queueUrl).messageBody(message));
        return "Sent: " + message;
    }
}
