package com.example.rabbitmqdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class RabbitmqDemoApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitmqDemoApplication.class);


    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = SpringApplication.run(RabbitmqDemoApplication.class, args);
    }


}
