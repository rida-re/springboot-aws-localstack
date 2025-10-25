package com.example.demo.controller;


import com.example.demo.config.AwsResourceInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/apigateway")
public class ApiGatewayController {

    @Autowired
    private AwsResourceInitializer initializer;

    @Autowired
    private ApiGatewayClient apiGatewayClient;

    /**
     * Create a new API Gateway (REST API)
     */
    @PostMapping("/create")
    public ResponseEntity<String> createApiGateway(@RequestParam String name) {
        CreateRestApiResponse response = apiGatewayClient.createRestApi(
                CreateRestApiRequest.builder()
                                    .name(name)
                                    .description("API created from Spring Boot via LocalStack")
                                    .build()
        );

        return ResponseEntity.ok("API Gateway created: " + response.id());
    }

    /**
     * List all API Gateways
     */
    @GetMapping("/list")
    public List<String> listApis() {
        return apiGatewayClient.getRestApis()
                               .items()
                               .stream()
                               .map(RestApi::id)
                               .toList();
    }

    /**
     * Delete all API Gateways
     */
    @DeleteMapping("/remove")
    public List<String> deleteAllApis() {
        List<String> deleted = new ArrayList<>();

        apiGatewayClient.getRestApis()
                        .items()
                        .forEach(api -> {
                            try {
                                apiGatewayClient.deleteRestApi(
                                        DeleteRestApiRequest.builder()
                                                            .restApiId(api.id())
                                                            .build()
                                );
                                deleted.add(api.name());
                            } catch (Exception e) {
                                System.err.println("Erreur lors de la suppression de l'API " + api.name() + ": " + e.getMessage());
                            }
                        });

        return deleted;
    }

    /**
     * Deploy an API Gateway (create a stage)
     */
    @PostMapping("/deploy")
    public ResponseEntity<String> deployApi(@RequestParam String apiId, @RequestParam String stageName) {
        CreateDeploymentResponse deployment = apiGatewayClient.createDeployment(
                CreateDeploymentRequest.builder()
                                       .restApiId(apiId)
                                       .stageName(stageName)
                                       .description("Deployed from Spring Boot + LocalStack")
                                       .build()
        );

        return ResponseEntity.ok("Deployment created with ID: " + deployment.id());
    }

    @PostMapping("/integrate")
    public ResponseEntity<String> integrateWithHttp(
            @RequestParam String apiId,
            @RequestParam String resourceId,
            @RequestParam(defaultValue = "GET") String method,
            @RequestParam String endpointUrl
    ) {
        try {
            apiGatewayClient.putIntegration(
                    PutIntegrationRequest.builder()
                                         .restApiId(apiId)
                                         .resourceId(resourceId)
                                         .httpMethod(method)
                                         .integrationHttpMethod(method)
                                         .type(IntegrationType.HTTP_PROXY)
                                         .uri(endpointUrl) // ex: http://host.docker.internal:8080/hello
                                         .build()
            );

            return ResponseEntity.ok("Integration created successfully!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                                 .body("Error creating integration: " + e.getMessage());
        }
    }

    /**
     * Create a resource + method under an existing API Gateway
     */
    @PostMapping("/resource")
    public ResponseEntity<String> createResourceAndMethod(@RequestParam String apiId,
                                                          @RequestParam String pathPart) {
        // Get the root resource ID
        String parentId = apiGatewayClient.getResources(
                GetResourcesRequest.builder()
                                   .restApiId(apiId)
                                   .build()
        )
                                          .items()
                                          .get(0)
                                          .id();

        // Create a new resource under root
        CreateResourceResponse resource = apiGatewayClient.createResource(
                CreateResourceRequest.builder()
                                     .restApiId(apiId)
                                     .parentId(parentId)
                                     .pathPart(pathPart)
                                     .build()
        );

        // Add a GET method to that resource
        apiGatewayClient.putMethod(
                PutMethodRequest.builder()
                                .restApiId(apiId)
                                .resourceId(resource.id())
                                .httpMethod("GET")
                                .authorizationType("NONE")
                                .build()
        );

        return ResponseEntity.ok("Resource created: /" + pathPart + " (method: GET)");
    }
}
