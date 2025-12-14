package net.younes.livraisonservice.service;

import lombok.RequiredArgsConstructor;
import net.younes.livraisonservice.dto.Customer;

import net.younes.livraisonservice.entite.Livraison;
import net.younes.livraisonservice.enumm.StatutLivraison;
import net.younes.livraisonservice.feign.CommandeRestClient;
import net.younes.livraisonservice.feign.CustomerRestClient;
import net.younes.livraisonservice.repository.LivraisonRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LivraisonService {

    private final LivraisonRepository livraisonRepository;
    private final CommandeRestClient commandeRestClient;
    private final CustomerRestClient customerRestClient;

    public Livraison createLivraison(Long commandeId, Long customerId) {
        Customer customer = customerRestClient.getCustomerById(customerId);

        Livraison livraison = Livraison.builder()
                .commandeId(commandeId)
                .customerId(customerId)
                .adresseLivraison("Adresse: " + customer.getName())
                .statut(StatutLivraison.EN_ATTENTE)
                .dateCreation(LocalDateTime.now())
                .build();

        return livraisonRepository.save(livraison);
    }

    public Livraison updateStatut(Long id, StatutLivraison statut) {
        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livraison non trouvée"));

        livraison.setStatut(statut);

        if (statut == StatutLivraison.LIVREE) {
            livraison.setDateLivraison(LocalDateTime.now());
        }

        return livraisonRepository.save(livraison);
    }

    public Livraison getLivraisonWithDetails(Long id) {
        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livraison non trouvée"));

        // Récupérer les infos de la commande et du customer
        livraison.setCommande(commandeRestClient.getCommandeById(livraison.getCommandeId()));
        livraison.setCustomer(customerRestClient.getCustomerById(livraison.getCustomerId()));

        return livraison;
    }

    public List<Livraison> getAllLivraisons() {
        return livraisonRepository.findAll();
    }

    public List<Livraison> getLivraisonsByCustomer(Long customerId) {
        return livraisonRepository.findByCustomerId(customerId);
    }

    public List<Livraison> getLivraisonsByStatut(StatutLivraison statut) {
        return livraisonRepository.findByStatut(statut);
    }
}
