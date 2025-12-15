package net.younes.commandeservice.controller;

import net.younes.commandeservice.entite.Commande;
import net.younes.commandeservice.entite.ProductItem;
import net.younes.commandeservice.dto.CommandeRequest;
import net.younes.commandeservice.dto.ProductItemRequest;
import net.younes.commandeservice.feign.CustomerRestClient;
import net.younes.commandeservice.feign.ProductRestClient;
import net.younes.commandeservice.feign.LivraisonRestClient;
import net.younes.commandeservice.repositories.CommandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/commandes")
public class CommandeRestController {

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private CustomerRestClient customerRestClient;

    @Autowired
    private ProductRestClient productRestClient;

    @Autowired
    private LivraisonRestClient livraisonRestClient;

    @GetMapping
    public List<Commande> getAllCommandes() {
        List<Commande> commandes = commandeRepository.findAll();
        commandes.forEach(this::enrichCommande);
        return commandes;
    }

    @GetMapping("/{id}")
    public Commande getCommande(@PathVariable Long id) {
        Commande cmd = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande not found with id: " + id));
        enrichCommande(cmd);
        return cmd;
    }

    @PostMapping
    public Commande createCommande(@RequestBody CommandeRequest request) {
        // Créer la commande
        Commande commande = Commande.builder()
                .customerId(request.getCustomerId())
                .productItems(new ArrayList<>())
                .build();

        // Créer les ProductItems et récupérer les prix depuis le produit-service
        for (ProductItemRequest itemRequest : request.getProductItems()) {
            try {
                // Récupérer le produit pour obtenir le prix
                var produit = productRestClient.getProductById(itemRequest.getProductId());
                
                ProductItem productItem = ProductItem.builder()
                        .productId(itemRequest.getProductId())
                        .quantity(itemRequest.getQuantity())
                        .unitPrice(produit.getPrice())
                        .cmd(commande)
                        .build();
                
                commande.getProductItems().add(productItem);
            } catch (Exception e) {
                System.err.println("⚠️ Erreur lors de la récupération du produit " + itemRequest.getProductId() + ": " + e.getMessage());
                throw new RuntimeException("Produit non trouvé: " + itemRequest.getProductId());
            }
        }

        // Sauvegarder la commande et ses ProductItems
        Commande saved = commandeRepository.save(commande);
        enrichCommande(saved); // remplir les objets Product et Customer
        
        // Créer automatiquement une livraison pour cette commande
        try {
            System.out.println("🔄 Tentative de création de livraison pour la commande #" + saved.getId() + " (customerId: " + saved.getCustomerId() + ")");
            Object livraison = livraisonRestClient.createLivraisonFromCommande(saved.getId());
            System.out.println("✅ Livraison créée automatiquement pour la commande #" + saved.getId() + " -> " + livraison);
        } catch (Exception e) {
            System.err.println("❌ ERREUR lors de la création automatique de la livraison pour la commande #" + saved.getId() + ": " + e.getMessage());
            e.printStackTrace();
            // Ne pas faire échouer la création de commande si la livraison échoue
            // La commande est déjà créée, on continue
        }
        
        return saved;
    }

    @PostMapping("/addProduct")
    public Commande createCommandeLegacy(@RequestBody Commande commande) {
        // Sauvegarder la commande et ses ProductItems
        Commande saved = commandeRepository.save(commande);
        enrichCommande(saved); // remplir les objets Product et Customer
        
        // Créer automatiquement une livraison pour cette commande
        try {
            livraisonRestClient.createLivraisonFromCommande(saved.getId());
            System.out.println("✅ Livraison créée automatiquement pour la commande #" + saved.getId());
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la création automatique de la livraison pour la commande #" + saved.getId() + ": " + e.getMessage());
        }
        
        return saved;
    }

    @DeleteMapping("/{id}")
    public void deleteCommande(@PathVariable Long id) {
        commandeRepository.deleteById(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<Commande> getCommandesByCustomer(@PathVariable Long customerId) {
        List<Commande> commandes = commandeRepository.findByCustomerId(customerId);
        commandes.forEach(this::enrichCommande);
        return commandes;
    }

    // 🔹 Méthode pour enrichir les commandes avec Customer et Product complet
    private void enrichCommande(Commande commande) {
        if (commande == null) return;
        
        // Remplir les infos client
        try {
            if (commande.getCustomerId() != null) {
                commande.setCustomer(customerRestClient.getCustomerById(commande.getCustomerId()));
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la récupération du client " + commande.getCustomerId() + ": " + e.getMessage());
            e.printStackTrace();
        }

        // Remplir chaque ProductItem avec le nom du produit
        if (commande.getProductItems() != null) {
            for (ProductItem pi : commande.getProductItems()) {
                try {
                    if (pi.getProductId() != null) {
                        // Récupérer le produit complet depuis le produit-service
                        var produit = productRestClient.getProductById(pi.getProductId());
                        
                        if (produit != null) {
                            // Assigner le produit complet (contient id, name, price, quantity)
                            pi.setProduct(produit);
                            
                            // S'assurer que le prix unitaire est correct
                            if (pi.getUnitPrice() == 0 && produit.getPrice() > 0) {
                                pi.setUnitPrice(produit.getPrice());
                            }
                            
                            System.out.println("✅ Produit enrichi: " + produit.getName() + " - Quantité: " + pi.getQuantity());
                        } else {
                            System.err.println("⚠️ Produit null pour productId: " + pi.getProductId());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur lors de la récupération du produit " + pi.getProductId() + ": " + e.getMessage());
                    e.printStackTrace();
                    // Continuer avec les autres produits même si celui-ci échoue
                }
            }
        }
    }
}
