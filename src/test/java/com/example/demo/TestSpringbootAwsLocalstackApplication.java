package com.example.demo;

import org.springframework.boot.SpringApplication;

public class TestSpringbootAwsLocalstackApplication {

	public static void main(String[] args) {
		SpringApplication.from(SpringbootAwsLocalstackApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
