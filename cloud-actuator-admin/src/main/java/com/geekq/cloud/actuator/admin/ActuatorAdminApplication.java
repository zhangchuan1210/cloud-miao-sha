package com.geekq.cloud.actuator.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAdminServer
@SpringBootApplication
public class ActuatorAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(ActuatorAdminApplication.class,args);
    }
}
