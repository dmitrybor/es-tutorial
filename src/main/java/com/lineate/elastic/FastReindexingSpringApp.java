package com.lineate.elastic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FastReindexingSpringApp {
    public static void main(String[] args) {
        SpringApplication.run(FastReindexingSpringApp.class, args);
    }
}
