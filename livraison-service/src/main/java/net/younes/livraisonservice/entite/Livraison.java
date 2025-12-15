package net.younes.livraisonservice.entite;

import jakarta.persistence.*;
import lombok.*;
import net.younes.livraisonservice.dto.Commande;
import net.younes.livraisonservice.dto.Customer;
import net.younes.livraisonservice.enumm.StatutLivraison;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Livraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long commandeId;

    private Long customerId;

    private String adresseLivraison;

    @Enumerated(EnumType.STRING)
    private StatutLivraison statut;

    private LocalDateTime dateLivraison;

    private LocalDateTime dateCreation;

    private String livreur;

    // Coordonnées GPS de l'adresse de livraison
    private Double latitude;
    private Double longitude;

    @Transient
    private Commande commande;

    @Transient
    private Customer customer;
}
