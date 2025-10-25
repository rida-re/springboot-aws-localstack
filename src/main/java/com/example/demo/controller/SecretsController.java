package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/secrets")
public class SecretsController {

    private final SecretsManagerClient secretsClient;

    public SecretsController(SecretsManagerClient secretsClient) {
        this.secretsClient = secretsClient;
    }

    // ✅ Create a secret
    @PostMapping
    public String createSecret(@RequestParam String name, @RequestBody String secretValue) {
        try {
            CreateSecretRequest request = CreateSecretRequest.builder()
                                                             .name(name)
                                                             .secretString(secretValue)
                                                             .build();
            CreateSecretResponse response = secretsClient.createSecret(request);
            return "Secret created with ARN: " + response.arn();
        } catch (ResourceExistsException e) {
            return "Secret already exists: " + name;
        }
    }

    // 📜 List all secrets
    @GetMapping
    public List<String> listSecrets() {
        ListSecretsResponse response = secretsClient.listSecrets();
        return response.secretList().stream()
                       .map(SecretListEntry::name)
                       .collect(Collectors.toList());
    }

    // ❌ Delete a secret
    @DeleteMapping("/{name}")
    public String deleteSecret(@PathVariable String name) {
        try {
            DeleteSecretRequest request = DeleteSecretRequest.builder()
                                                             .secretId(name)
                                                             .forceDeleteWithoutRecovery(true) // skip recovery waiting period
                                                             .build();
            secretsClient.deleteSecret(request);
            return "Secret deleted: " + name;
        } catch (ResourceNotFoundException e) {
            return "Secret not found: " + name;
        }
    }
}
