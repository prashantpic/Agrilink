```java
package com.thesss.platform.workflows;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableProcessApplication // Recommended for Camunda Spring Boot Starter
@EnableFeignClients(basePackages = "com.thesss.platform.workflows.client")
public class ApprovalWorkflowOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApprovalWorkflowOrchestratorApplication.class, args);
    }

}
```