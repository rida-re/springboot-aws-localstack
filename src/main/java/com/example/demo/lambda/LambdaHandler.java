package com.example.demo.lambda;

import java.util.Map;

public class LambdaHandler {


    public String handleRequest(Map<String, String> event) {
        String name = event.getOrDefault("name", "World");
        return "Hello, " + name + "!";
    }
}
