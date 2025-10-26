package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// --- ADD THESE IMPORTS ---
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
// --- END IMPORTS ---

@SpringBootApplication
// --- THIS IS THE FIX: Removed ".*" from the basePackages ---
@ComponentScan(basePackages = {"com.example.demo"})
@EntityScan(basePackages = {"com.example.demo.model"})
@EnableJpaRepositories(basePackages = {"com.example.demo.repository"})
// --- END ANNOTATIONS ---
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

