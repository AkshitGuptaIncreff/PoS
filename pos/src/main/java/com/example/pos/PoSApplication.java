package com.example.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.example.pos",
        "com.example.invoice"
})
@EnableScheduling
public class PoSApplication {
    public static void main(String[] args) {
        SpringApplication.run(PoSApplication.class, args);
    }
}