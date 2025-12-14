package net.younes.clientservice;

import net.younes.clientservice.entites.Customer;
import net.younes.clientservice.repositories.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ClientServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(CustomerRepository customerRepository) {
        return args -> {
            customerRepository.save(Customer.builder()
                    .name("Younes")
                    .email("Younestaje@gmail.com").build());
            customerRepository.save(Customer.builder()
                    .name("Fouad")
                    .email("Fouad@gmail.com").build());
            customerRepository.save(Customer.builder()
                    .name("Hasnaa")
                    .email("Hasnaa@gmail.com").build());
            customerRepository.findAll().forEach(c->{
                System.out.println("=============");
                System.out.println(c.getId());
                System.out.println(c.getName());
                System.out.println(c.getEmail());
                System.out.println("=============");
            });

        };

    }

}
