package net.younes.livraisonservice.controller;



import lombok.RequiredArgsConstructor;
import net.younes.livraisonservice.entite.Livraison;
import net.younes.livraisonservice.enumm.StatutLivraison;

import net.younes.livraisonservice.service.LivraisonService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@Configuration

@RestController
@RequestMapping("/livraisons")
@RequiredArgsConstructor
@CrossOrigin
public class LivraisonRestController {

    private final LivraisonService livraisonService;

    @GetMapping
    public List<Livraison> getAllLivraisons() {
        System.out.println("📥 GET /livraisons - Récupération de toutes les livraisons");
        List<Livraison> livraisons = livraisonService.getAllLivraisons();
        System.out.println("✅ Nombre de livraisons retournées: " + livraisons.size());
        return livraisons;
    }

    @GetMapping("/{id}")
    public Livraison getLivraison(@PathVariable Long id) {
        return livraisonService.getLivraisonWithDetails(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<Livraison> getLivraisonsByCustomer(@PathVariable Long customerId) {
        return livraisonService.getLivraisonsByCustomer(customerId);
    }

    @PostMapping
    public Livraison createLivraison(@RequestParam Long commandeId, @RequestParam(required = false) Long customerId) {
        // Si customerId n'est pas fourni, le récupérer depuis la commande
        if (customerId == null) {
            net.younes.livraisonservice.dto.Commande commande = livraisonService.getCommandeById(commandeId);
            if (commande == null) {
                throw new RuntimeException("Commande non trouvée");
            }
            customerId = commande.getCustomerId();
        }
        return livraisonService.createLivraison(commandeId, customerId);
    }
    
    @PostMapping("/{commandeId}")
    public Livraison createLivraisonFromCommande(@PathVariable Long commandeId) {
        net.younes.livraisonservice.dto.Commande commande = livraisonService.getCommandeById(commandeId);
        if (commande == null) {
            throw new RuntimeException("Commande non trouvée avec l'ID: " + commandeId);
        }
        return livraisonService.createLivraison(commandeId, commande.getCustomerId());
    }

    @PutMapping("/{id}/statut")
    public Livraison updateStatut(@PathVariable Long id, @RequestParam StatutLivraison statut) {
        return livraisonService.updateStatut(id, statut);
    }

    @PostMapping("/create-for-existing-commandes")
    public String createLivraisonsForExistingCommandes() {
        return livraisonService.createLivraisonsForExistingCommandes();
    }

    @GetMapping("/{id}/coordinates")
    public Map<String, Double> getCoordinates(@PathVariable Long id) {
        Livraison livraison = livraisonService.getLivraisonWithDetails(id);
        if (livraison.getLatitude() != null && livraison.getLongitude() != null) {
            return Map.of(
                "latitude", livraison.getLatitude(),
                "longitude", livraison.getLongitude()
            );
        }
        return Map.of();
    }
}

