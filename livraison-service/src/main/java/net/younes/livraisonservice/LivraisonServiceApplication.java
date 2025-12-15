package net.younes.livraisonservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class LivraisonServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LivraisonServiceApplication.class, args);
    }
}