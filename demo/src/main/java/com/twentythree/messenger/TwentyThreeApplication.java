package com.twentythree.messenger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // For created_at, updated_at

@SpringBootApplication
@EnableJpaAuditing // Enable JPA Auditing
public class TwentyThreeApplication {
    public static void main(String[] args) {
        SpringApplication.run(TwentyThreeApplication.class, args);
    }
}