package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.CreateDbInstanceRequest;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesRequest;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;

import java.util.Map;

@RestController
@RequestMapping("/rds")
public class RdsController {

    private final RdsClient rdsClient;

    @Value("${aws.rds.instance}")
    private String rdsInstanceId;

    @Value("${aws.rds.dbName}")
    private String rdsDbName;

    @Value("${aws.rds.username}")
    private String rdsUsername;

    @Value("${aws.rds.password}")
    private String rdsPassword;

    public RdsController(RdsClient rdsClient) {
        this.rdsClient = rdsClient;
    }

    @PostMapping("/create")
    public String create() {
        rdsClient.createDBInstance(CreateDbInstanceRequest.builder()
                .dbInstanceIdentifier(rdsInstanceId)
                .engine("postgres")
                .engineVersion("12")
                .dbInstanceClass("db.t3.micro")
                .allocatedStorage(20)
                .masterUsername(rdsUsername)
                .masterUserPassword(rdsPassword)
                .dbName(rdsDbName)
                .build());
        return "Creating RDS instance: " + rdsInstanceId;
    }

    @GetMapping("/status")
    public String status() {
        DescribeDbInstancesResponse resp = rdsClient.describeDBInstances(DescribeDbInstancesRequest.builder()
                .dbInstanceIdentifier(rdsInstanceId)
                .build());
        DBInstance db = resp.dbInstances().get(0);
        return db.dbInstanceStatus();
    }

    @GetMapping("/endpoint")
    public Map<String, Object> endpoint() {
        DescribeDbInstancesResponse resp = rdsClient.describeDBInstances(DescribeDbInstancesRequest.builder()
                .dbInstanceIdentifier(rdsInstanceId)
                .build());
        DBInstance db = resp.dbInstances().get(0);
        return Map.of(
                "address", db.endpoint().address(),
                "port", db.endpoint().port(),
                "dbName", rdsDbName
        );
    }
}
