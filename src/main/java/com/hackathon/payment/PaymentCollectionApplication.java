package com.hackathon.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PaymentCollectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentCollectionApplication.class, args);
    }
}
