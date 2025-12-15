package net.younes.livraisonservice.feign;

import net.younes.livraisonservice.dto.Commande;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "commande-service")
public interface CommandeRestClient {

    @GetMapping("/api/commandes")
    List<Commande> getAllCommandes();

    @GetMapping("/api/commandes/{id}")
    Commande getCommandeById(@PathVariable Long id);
}
