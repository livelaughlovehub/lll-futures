package com.lll.futures;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FuturesApplication {
    public static void main(String[] args) {
        SpringApplication.run(FuturesApplication.class, args);
    }
}


