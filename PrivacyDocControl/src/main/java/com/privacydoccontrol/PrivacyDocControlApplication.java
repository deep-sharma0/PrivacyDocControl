package com.privacydoccontrol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableScheduling
@SpringBootApplication
public class PrivacyDocControlApplication {
    public static void main(String[] args) {
        SpringApplication.run(PrivacyDocControlApplication.class, args);
    }
}
