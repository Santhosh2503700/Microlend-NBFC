package com.microlend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class MicroLendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroLendApplication.class, args);
    }
}
