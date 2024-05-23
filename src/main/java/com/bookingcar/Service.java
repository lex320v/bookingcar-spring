package com.bookingcar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Service {

    public static void main(String[] args) {
        SpringApplication.run(Service.class, args);
    }
}
