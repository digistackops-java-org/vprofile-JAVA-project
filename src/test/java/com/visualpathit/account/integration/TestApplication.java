package com.visualpathit.account.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Test Spring Boot Application
 * Used for integration and API tests
 * 
 * If you already have a main Application class in your project,
 * you can use that instead by updating the @SpringBootTest annotation
 * in the test classes
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.visualpathit.account")
public class TestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
