package com.example.onlinetestingbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })//暂时排除login
public class OnlineTestingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineTestingBackendApplication.class, args);
    }

}
