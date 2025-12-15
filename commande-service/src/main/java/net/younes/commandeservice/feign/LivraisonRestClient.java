package net.younes.commandeservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "livraison-service")
public interface LivraisonRestClient {

    @PostMapping("/livraisons/{commandeId}")
    Object createLivraisonFromCommande(@PathVariable Long commandeId);
}

