package net.younes.commandeservice.feign;

import net.younes.commandeservice.dto.Customer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "client-service", url = "http://localhost:8081")
public interface CustomerRestClient {

    @GetMapping("/api/customers")
    List<Customer> getAllCustomers();

    @GetMapping("/api/customers/{id}")
    Customer getCustomerById(@PathVariable Long id);
}