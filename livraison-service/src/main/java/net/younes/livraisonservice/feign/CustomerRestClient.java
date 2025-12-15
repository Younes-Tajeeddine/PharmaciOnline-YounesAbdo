package net.younes.livraisonservice.feign;

import net.younes.livraisonservice.dto.Customer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "client-service")
public interface CustomerRestClient {

    @GetMapping("/api/customers/{id}")
    Customer getCustomerById(@PathVariable Long id);
}
