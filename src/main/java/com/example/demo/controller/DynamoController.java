package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dynamo")
public class DynamoController {

    private final DynamoDbClient dynamoDbClient;

    @Value("${aws.dynamodb.table}")
    private String tableName;

    public DynamoController(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @PostMapping("/add")
    public String addItem(@RequestParam String id, @RequestParam String name) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(id).build());
        item.put("name", AttributeValue.builder().s(name).build());

        dynamoDbClient.putItem(PutItemRequest.builder().tableName(tableName).item(item).build());
        return "Added: " + id;
    }

    @GetMapping("/get")
    public Map<String, AttributeValue> getItem(@RequestParam String id) {
        Map<String, AttributeValue> key = Map.of("id", AttributeValue.builder().s(id).build());
        return dynamoDbClient.getItem(GetItemRequest.builder().tableName(tableName).key(key).build()).item();
    }
}
