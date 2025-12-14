package net.younes.commandeservice.controller;

import net.younes.commandeservice.entite.Commande;
import net.younes.commandeservice.entite.ProductItem;
import net.younes.commandeservice.feign.CustomerRestClient;
import net.younes.commandeservice.feign.ProductRestClient;
import net.younes.commandeservice.repositories.CommandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/addProduct")
    public Commande createCommande(@RequestBody Commande commande) {
        // Sauvegarder la commande et ses ProductItems
        Commande saved = commandeRepository.save(commande);
        enrichCommande(saved); // remplir les objets Product et Customer
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
        try {
            // Remplir les infos client
            commande.setCustomer(customerRestClient.getCustomerById(commande.getCustomerId()));

            // Remplir chaque ProductItem
            for (ProductItem pi : commande.getProductItems()) {
                var produit = productRestClient.getProductById(pi.getProductId());
                pi.setProduct(produit);           // remplir nom, price
                pi.setUnitPrice(produit.getPrice()); // s'assurer que unitPrice est correct
            }

        } catch (Exception e) {
            System.err.println("⚠️ Impossible d’enrichir la commande : " + e.getMessage());
        }
    }
}
