package com.example.demo.controller;

import com.example.demo.config.AwsResourceInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.DeleteFunctionRequest;
import software.amazon.awssdk.services.lambda.model.FunctionConfiguration;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/lambda")
public class LambdaController {

    @Autowired
    private AwsResourceInitializer initializer;

    @Autowired
    private LambdaClient lambdaClient;

    @PostMapping("/create")
    public ResponseEntity<String> createLambda() {
        initializer.createLambdaFunction();
        return ResponseEntity.ok("Lambda creation triggered");
    }

    @GetMapping("/list")
    public List<String> listFunctions() {
        return lambdaClient.listFunctions().functions()
                           .stream().map(FunctionConfiguration::functionName).toList();
    }

    @DeleteMapping("/remove")
    public List<String> removeFunctions() {
        List<String> deletedFunctions = new ArrayList<>();

        lambdaClient.listFunctions().functions()
                    .forEach(function -> {
                        try {
                            lambdaClient.deleteFunction(DeleteFunctionRequest.builder()
                                                                             .functionName(function.functionName())
                                                                             .build());
                            deletedFunctions.add(function.functionName());
                        } catch (Exception e) {
                            // Log l'erreur mais continue avec les autres fonctions
                            System.err.println("Erreur lors de la suppression de " +
                                    function.functionName() + ": " + e.getMessage());
                        }
                    });

        return deletedFunctions;
    }

    @PostMapping("/invoke")
    public ResponseEntity<String> invokeLambda(@RequestParam String functionName, @RequestBody String payload) {
        InvokeResponse response = lambdaClient.invoke(InvokeRequest.builder()
                                                                   .functionName(functionName)
                                                                   .payload(SdkBytes.fromUtf8String(payload))
                                                                   .build());
        return ResponseEntity.ok(response.payload().asUtf8String());
    }
}
