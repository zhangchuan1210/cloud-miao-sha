package com.geekq.cloud.reuka.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class CloudReukaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudReukaServiceApplication.class, args);
    }

}
