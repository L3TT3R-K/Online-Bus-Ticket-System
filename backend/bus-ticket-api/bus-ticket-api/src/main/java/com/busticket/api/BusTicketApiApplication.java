package com.busticket.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BusTicketApiApplication {

    public static void main(String[] args) {
        
        SpringApplication.run(BusTicketApiApplication.class, args);
    }

}
