package net.younes.livraisonservice.repository;



import net.younes.livraisonservice.entite.Livraison;
import net.younes.livraisonservice.enumm.StatutLivraison;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LivraisonRepository extends JpaRepository<Livraison, Long> {
    List<Livraison> findByCustomerId(Long customerId);
    List<Livraison> findByStatut(StatutLivraison statut);
    List<Livraison> findByCommandeId(Long commandeId);
}
