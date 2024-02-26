package com.monitorclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class MonitorClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorClientApplication.class, args);
    }

}
