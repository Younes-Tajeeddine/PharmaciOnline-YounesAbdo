package net.younes.clientservice.controller;

import net.younes.clientservice.entites.Customer;
import net.younes.clientservice.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin("http://localhost:3000")
public class CustomerRestController {

    @Autowired
    private CustomerRepository repo;

    @GetMapping
    public List<Customer> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Customer getById(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    @PostMapping
    public Customer save(@RequestBody Customer customer) {
        return repo.save(customer);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
