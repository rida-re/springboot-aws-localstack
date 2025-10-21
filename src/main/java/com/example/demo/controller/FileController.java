package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public FileController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        s3Client.putObject(
                PutObjectRequest.builder().bucket(bucketName).key(file.getOriginalFilename()).build(),
                RequestBody.fromBytes(file.getBytes())
        );
        return "File uploaded: " + file.getOriginalFilename();
    }

    @GetMapping("/list")
    public List<String> listFiles() {
        ListObjectsRequest listObjects = ListObjectsRequest.builder().bucket(bucketName).build();
        return s3Client.listObjects(listObjects)
                       .contents()
                       .stream()
                       .map(S3Object::key)
                       .toList();
    }
}
