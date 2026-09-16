package com.manish.b2bmonitor;

import com.manish.b2bmonitor.as2.As2Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(As2Properties.class)
public class B2bTransactionObservabilityApplication {
    public static void main(String[] args) {
        SpringApplication.run(B2bTransactionObservabilityApplication.class, args);
    }
}
