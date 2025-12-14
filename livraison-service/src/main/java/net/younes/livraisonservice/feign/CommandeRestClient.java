package net.younes.livraisonservice.feign;

import net.younes.livraisonservice.dto.Commande;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "commande-service", url = "http://localhost:8082")
public interface CommandeRestClient {

    @GetMapping("/commandes/{id}")
    Commande getCommandeById(@PathVariable Long id);
}