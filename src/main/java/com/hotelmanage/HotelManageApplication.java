package com.hotelmanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HotelManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelManageApplication.class, args);
    }

}
