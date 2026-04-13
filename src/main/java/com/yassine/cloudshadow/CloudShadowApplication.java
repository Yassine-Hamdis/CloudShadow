package com.yassine.cloudshadow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CloudShadowApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudShadowApplication.class, args);
    }

}
