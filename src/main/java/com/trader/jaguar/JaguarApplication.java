package com.trader.jaguar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class JaguarApplication {

    public static void main(String[] args) {
        SpringApplication.run(JaguarApplication.class, args);
        log.info("XXXX Jaguar Started XXXX");
    }

}
