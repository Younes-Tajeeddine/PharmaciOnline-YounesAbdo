package net.younes.livraisonservice.service;

import lombok.RequiredArgsConstructor;
import net.younes.livraisonservice.dto.Customer;

import net.younes.livraisonservice.entite.Livraison;
import net.younes.livraisonservice.enumm.StatutLivraison;
import net.younes.livraisonservice.feign.CommandeRestClient;
import net.younes.livraisonservice.feign.CustomerRestClient;
import net.younes.livraisonservice.repository.LivraisonRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LivraisonService {

    private final LivraisonRepository livraisonRepository;
    private final CommandeRestClient commandeRestClient;
    private final CustomerRestClient customerRestClient;
    private final GeolocationService geolocationService;

    public Livraison createLivraison(Long commandeId, Long customerId) {
        System.out.println("🔄 LivraisonService.createLivraison - commandeId: " + commandeId + ", customerId: " + customerId);
        
        // Récupérer la commande pour vérifier qu'elle existe
        net.younes.livraisonservice.dto.Commande commande = commandeRestClient.getCommandeById(commandeId);
        if (commande == null) {
            throw new RuntimeException("Commande non trouvée avec l'ID: " + commandeId);
        }
        System.out.println("✅ Commande récupérée: " + commande.getId() + " (customerId: " + commande.getCustomerId() + ")");

        // Utiliser le customerId de la commande si celui fourni est null
        if (customerId == null && commande.getCustomerId() != null) {
            customerId = commande.getCustomerId();
            System.out.println("🔄 Utilisation du customerId de la commande: " + customerId);
        }

        // Récupérer le client pour obtenir l'adresse
        Customer customer = customerRestClient.getCustomerById(customerId);
        if (customer == null) {
            throw new RuntimeException("Client non trouvé avec l'ID: " + customerId);
        }
        System.out.println("✅ Client récupéré: " + customer.getName());

        // Utiliser l'adresse du client si disponible, sinon utiliser un message par défaut
        String adresse = customer.getAddress() != null && !customer.getAddress().isEmpty() 
            ? customer.getAddress() 
            : (customer.getName() != null ? "Adresse de " + customer.getName() : "Adresse non spécifiée");

        // Géolocaliser l'adresse
        Double latitude = null;
        Double longitude = null;
        if (geolocationService.isGeolocalizable(adresse)) {
            try {
                Map<String, Double> coordinates = geolocationService.getCoordinates(adresse);
                if (coordinates != null) {
                    latitude = coordinates.get("latitude");
                    longitude = coordinates.get("longitude");
                    System.out.println("🌍 Coordonnées GPS récupérées: lat=" + latitude + ", lon=" + longitude);
                } else {
                    System.out.println("⚠️ Impossible de géolocaliser l'adresse: " + adresse);
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la géolocalisation: " + e.getMessage());
                // Ne pas faire échouer la création de livraison si la géolocalisation échoue
            }
        }

        Livraison livraison = Livraison.builder()
                .commandeId(commandeId)
                .customerId(customerId)
                .adresseLivraison(adresse)
                .statut(StatutLivraison.EN_PREPARATION)
                .dateCreation(LocalDateTime.now())
                .livreur("À assigner")
                .latitude(latitude)
                .longitude(longitude)
                .build();

        Livraison saved = livraisonRepository.save(livraison);
        System.out.println("✅ Livraison sauvegardée avec succès: ID=" + saved.getId() + ", Statut=" + saved.getStatut());
        if (latitude != null && longitude != null) {
            System.out.println("📍 Coordonnées GPS: " + latitude + ", " + longitude);
        }
        return saved;
    }

    public Livraison updateStatut(Long id, StatutLivraison statut) {
        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livraison non trouvée"));

        livraison.setStatut(statut);

        if (statut == StatutLivraison.LIVREE) {
            livraison.setDateLivraison(LocalDateTime.now());
        }

        return livraisonRepository.save(livraison);
    }

    public Livraison getLivraisonWithDetails(Long id) {
        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livraison non trouvée"));

        // Récupérer les infos de la commande
        if (livraison.getCommandeId() != null) {
            try {
                net.younes.livraisonservice.dto.Commande commande = commandeRestClient.getCommandeById(livraison.getCommandeId());
                if (commande != null) {
                    livraison.setCommande(commande);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Erreur lors de la récupération de la commande: " + e.getMessage());
            }
        }

        // Récupérer les infos du customer - CRITIQUE : ne jamais laisser null
        if (livraison.getCustomerId() != null) {
            try {
                Customer customer = customerRestClient.getCustomerById(livraison.getCustomerId());
                if (customer != null) {
                    livraison.setCustomer(customer);
                } else {
                    // Créer un customer par défaut
                    Customer defaultCustomer = new Customer();
                    defaultCustomer.setId(livraison.getCustomerId());
                    defaultCustomer.setName("Client inconnu (ID: " + livraison.getCustomerId() + ")");
                    defaultCustomer.setEmail("N/A");
                    livraison.setCustomer(defaultCustomer);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Erreur lors de la récupération du customer: " + e.getMessage());
                // Créer un customer par défaut en cas d'erreur
                Customer defaultCustomer = new Customer();
                defaultCustomer.setId(livraison.getCustomerId());
                defaultCustomer.setName("Erreur de chargement");
                defaultCustomer.setEmail("N/A");
                livraison.setCustomer(defaultCustomer);
            }
        } else {
            // Créer un customer par défaut si customerId est null
            Customer defaultCustomer = new Customer();
            defaultCustomer.setId(-1L);
            defaultCustomer.setName("Client non spécifié");
            defaultCustomer.setEmail("N/A");
            livraison.setCustomer(defaultCustomer);
        }

        return livraison;
    }

    public List<Livraison> getAllLivraisons() {
        List<Livraison> livraisons = livraisonRepository.findAll();
        // Enrichir toutes les livraisons avec les détails de commande et client
        livraisons.forEach(livraison -> {
            try {
                // Toujours enrichir la commande
                if (livraison.getCommandeId() != null) {
                    net.younes.livraisonservice.dto.Commande commande = commandeRestClient.getCommandeById(livraison.getCommandeId());
                    if (commande != null) {
                        livraison.setCommande(commande);
                    } else {
                        System.err.println("⚠️ Commande non trouvée pour la livraison " + livraison.getId());
                    }
                }
                
                // Toujours enrichir le customer - CRITIQUE : ne jamais laisser null
                if (livraison.getCustomerId() != null) {
                    Customer customer = customerRestClient.getCustomerById(livraison.getCustomerId());
                    if (customer != null) {
                        livraison.setCustomer(customer);
                        System.out.println("✅ Customer enrichi pour livraison " + livraison.getId() + ": " + customer.getName());
                    } else {
                        System.err.println("❌ ERREUR: Customer non trouvé pour la livraison " + livraison.getId() + " (customerId: " + livraison.getCustomerId() + ")");
                        // Créer un customer par défaut pour éviter null
                        Customer defaultCustomer = new Customer();
                        defaultCustomer.setId(livraison.getCustomerId());
                        defaultCustomer.setName("Client inconnu (ID: " + livraison.getCustomerId() + ")");
                        defaultCustomer.setEmail("N/A");
                        livraison.setCustomer(defaultCustomer);
                    }
                } else {
                    System.err.println("❌ ERREUR: customerId est null pour la livraison " + livraison.getId());
                    // Créer un customer par défaut
                    Customer defaultCustomer = new Customer();
                    defaultCustomer.setId(-1L);
                    defaultCustomer.setName("Client non spécifié");
                    defaultCustomer.setEmail("N/A");
                    livraison.setCustomer(defaultCustomer);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Erreur lors de l'enrichissement de la livraison " + livraison.getId() + ": " + e.getMessage());
                e.printStackTrace();
                // Créer un customer par défaut en cas d'erreur
                if (livraison.getCustomer() == null) {
                    Customer defaultCustomer = new Customer();
                    defaultCustomer.setId(livraison.getCustomerId() != null ? livraison.getCustomerId() : -1L);
                    defaultCustomer.setName("Erreur de chargement");
                    defaultCustomer.setEmail("N/A");
                    livraison.setCustomer(defaultCustomer);
                }
            }
        });
        return livraisons;
    }

    public List<Livraison> getLivraisonsByCustomer(Long customerId) {
        return livraisonRepository.findByCustomerId(customerId);
    }

    public List<Livraison> getLivraisonsByStatut(StatutLivraison statut) {
        return livraisonRepository.findByStatut(statut);
    }

    public net.younes.livraisonservice.dto.Commande getCommandeById(Long commandeId) {
        return commandeRestClient.getCommandeById(commandeId);
    }

    public String createLivraisonsForExistingCommandes() {
        try {
            System.out.println("🔄 Début de la création de livraisons pour les commandes existantes...");
            
            // Récupérer toutes les commandes
            List<net.younes.livraisonservice.dto.Commande> commandes = commandeRestClient.getAllCommandes();
            System.out.println("📋 Nombre de commandes trouvées: " + commandes.size());
            
            int created = 0;
            int skipped = 0;
            int errors = 0;
            
            for (net.younes.livraisonservice.dto.Commande commande : commandes) {
                try {
                    // Vérifier si une livraison existe déjà pour cette commande
                    List<Livraison> existingLivraisons = livraisonRepository.findByCommandeId(commande.getId());
                    
                    if (existingLivraisons != null && !existingLivraisons.isEmpty()) {
                        System.out.println("⏭️ Livraison déjà existante pour la commande #" + commande.getId());
                        skipped++;
                        continue;
                    }
                    
                    // Créer la livraison
                    if (commande.getCustomerId() != null) {
                        createLivraison(commande.getId(), commande.getCustomerId());
                        created++;
                        System.out.println("✅ Livraison créée pour la commande #" + commande.getId());
                    } else {
                        System.err.println("⚠️ Commande #" + commande.getId() + " n'a pas de customerId, impossible de créer la livraison");
                        errors++;
                    }
                } catch (Exception e) {
                    System.err.println("❌ Erreur lors de la création de la livraison pour la commande #" + commande.getId() + ": " + e.getMessage());
                    errors++;
                }
            }
            
            String result = String.format("✅ Terminé: %d livraisons créées, %d ignorées (déjà existantes), %d erreurs", created, skipped, errors);
            System.out.println(result);
            return result;
        } catch (Exception e) {
            String error = "❌ Erreur lors de la création des livraisons: " + e.getMessage();
            System.err.println(error);
            e.printStackTrace();
            return error;
        }
    }
}
