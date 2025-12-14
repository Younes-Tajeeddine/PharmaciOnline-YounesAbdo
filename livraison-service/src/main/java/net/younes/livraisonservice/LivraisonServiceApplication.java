
package net.younes.livraisonservice;


import net.younes.livraisonservice.entite.Livraison;
import net.younes.livraisonservice.enumm.StatutLivraison;
import net.younes.livraisonservice.repository.LivraisonRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableFeignClients
public class LivraisonServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LivraisonServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner init(LivraisonRepository livraisonRepository) {
        return args -> {
            // Créer des livraisons de test
            livraisonRepository.save(Livraison.builder()
                    .commandeId(1L)
                    .customerId(1L)
                    .adresseLivraison("Rue Hassan II, Casablanca")
                    .statut(StatutLivraison.EN_COURS)
                    .dateCreation(LocalDateTime.now())
                    .livreur("Ahmed")
                    .build());

            livraisonRepository.save(Livraison.builder()
                    .commandeId(2L)
                    .customerId(2L)
                    .adresseLivraison("Avenue Mohammed V, Rabat")
                    .statut(StatutLivraison.EN_PREPARATION)
                    .dateCreation(LocalDateTime.now())
                    .livreur("Fatima")
                    .build());

            livraisonRepository.save(Livraison.builder()
                    .commandeId(3L)
                    .customerId(3L)
                    .adresseLivraison("Boulevard Zerktouni, Marrakech")
                    .statut(StatutLivraison.LIVREE)
                    .dateCreation(LocalDateTime.now().minusDays(1))
                    .dateLivraison(LocalDateTime.now())
                    .livreur("Youssef")
                    .build());

            System.out.println("✅ Livraisons créées avec succès !");

            // Afficher les livraisons
            livraisonRepository.findAll().forEach(livraison -> {
                System.out.println(livraison.toString());
            });
        };
    }
}