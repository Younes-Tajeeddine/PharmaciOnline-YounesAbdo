package net.younes.commandeservice.entite;

import jakarta.persistence.*;
import lombok.*;
import net.younes.commandeservice.dto.Customer;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    @OneToMany(mappedBy = "cmd", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductItem> productItems = new ArrayList<>();

    @Transient
    private Customer customer;
}