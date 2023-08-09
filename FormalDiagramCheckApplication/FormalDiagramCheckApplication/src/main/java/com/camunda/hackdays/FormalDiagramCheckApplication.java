package com.camunda.hackdays;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FormalDiagramCheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(FormalDiagramCheckApplication.class, args);
    }
}
