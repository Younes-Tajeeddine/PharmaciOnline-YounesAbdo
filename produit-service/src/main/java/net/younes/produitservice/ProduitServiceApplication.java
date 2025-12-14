package net.younes.produitservice;

import net.younes.produitservice.entites.Product;
import net.younes.produitservice.repositories.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProduitServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProduitServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ProductRepository productRepository) {
        return args -> {
            // SUPPRIMEZ .id() - il sera généré automatiquement
            productRepository.save(Product.builder()
                    .name("Paracétamol 500mg")
                    .price(15.50)
                    .quantity(100)
                    .build());

            productRepository.save(Product.builder()
                    .name("Ibuprofène 400mg")
                    .price(25.00)
                    .quantity(80)
                    .build());

            productRepository.save(Product.builder()
                    .name("Vitamine C 1000mg")
                    .price(45.00)
                    .quantity(60)
                    .build());

            productRepository.save(Product.builder()
                    .name("Aspirine 100mg")
                    .price(12.00)
                    .quantity(120)
                    .build());

            productRepository.save(Product.builder()
                    .name("Doliprane 1000mg")
                    .price(18.50)
                    .quantity(90)
                    .build());

            System.out.println("✅ Produits créés avec succès !");

            productRepository.findAll().forEach(p -> {
                System.out.println(p.toString());
            });
        };
    }
}