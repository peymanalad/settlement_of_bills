package com.sepehrnet.settlement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@PropertySource("classpath:config.properties")
@ConfigurationPropertiesScan("com.sepehrnet.settlement.config")
@EnableScheduling
public class SettlementOfBillsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettlementOfBillsApplication.class, args);
    }

}
