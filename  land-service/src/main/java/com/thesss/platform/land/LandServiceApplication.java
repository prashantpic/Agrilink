```java
package com.thesss.platform.land;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // REQ-2-018: Enables JPA Auditing for createdBy, createdDate, etc.
@ConfigurationPropertiesScan // To scan for @ConfigurationProperties beans like AppProperties
public class LandServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LandServiceApplication.class, args);
    }

}
```