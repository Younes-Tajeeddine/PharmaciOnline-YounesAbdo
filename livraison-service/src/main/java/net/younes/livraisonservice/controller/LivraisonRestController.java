package net.younes.livraisonservice.controller;



import lombok.RequiredArgsConstructor;
import net.younes.livraisonservice.entite.Livraison;
import net.younes.livraisonservice.enumm.StatutLivraison;

import net.younes.livraisonservice.service.LivraisonService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/livraisons")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // Pour React
public class LivraisonRestController {

    private final LivraisonService livraisonService;

    @GetMapping
    public List<Livraison> getAllLivraisons() {
        return livraisonService.getAllLivraisons();
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
    public Livraison createLivraison(@RequestParam Long commandeId, @RequestParam Long customerId) {
        return livraisonService.createLivraison(commandeId, customerId);
    }

    @PutMapping("/{id}/statut")
    public Livraison updateStatut(@PathVariable Long id, @RequestParam StatutLivraison statut) {
        return livraisonService.updateStatut(id, statut);
    }
}

