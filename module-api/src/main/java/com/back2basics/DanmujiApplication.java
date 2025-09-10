package com.back2basics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.back2basics")
@EntityScan("com.back2basics")
@EnableJpaRepositories(basePackages = "com.back2basics")
@EnableMongoRepositories(basePackages = "com.back2basics")
@EnableScheduling
@EnableCaching
public class DanmujiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DanmujiApplication.class, args);
    }
}