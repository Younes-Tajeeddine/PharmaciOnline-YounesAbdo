package net.younes.livraisonservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class GeolocationService {

    private final WebClient webClient;
    
    @Value("${geolocation.api.url:https://nominatim.openstreetmap.org/search}")
    private String geolocationApiUrl;
    
    @Value("${geolocation.api.timeout:5000}")
    private int timeout;

    public GeolocationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader("User-Agent", "PharmacyDeliveryApp/1.0")
                .build();
    }

    /**
     * Récupère les coordonnées GPS (latitude, longitude) d'une adresse
     * @param address L'adresse à géolocaliser
     * @return Map contenant "latitude" et "longitude", ou null en cas d'erreur
     */
    public Map<String, Double> getCoordinates(String address) {
        if (address == null || address.trim().isEmpty()) {
            log.warn("⚠️ Adresse vide, impossible de géolocaliser");
            return null;
        }

        try {
            log.info("🌍 Tentative de géolocalisation pour l'adresse: {}", address);

            // Utilisation de l'API Nominatim d'OpenStreetMap (gratuite, sans clé API)
            Map<String, String> params = new HashMap<>();
            params.put("q", address);
            params.put("format", "json");
            params.put("limit", "1");

            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", address)
                            .queryParam("format", "json")
                            .queryParam("limit", "1")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            log.debug("📥 Réponse de l'API de géolocalisation: {}", response);

            // Parser la réponse JSON de Nominatim
            // Format: [{"place_id":...,"lat":"48.8566","lon":"2.3522",...}]
            if (response != null && response.startsWith("[") && response.length() > 2) {
                // Extraire le premier objet JSON du tableau
                String firstObject = extractFirstObject(response);
                
                if (firstObject != null) {
                    // Extraire lat et lon (peuvent être des strings ou des nombres)
                    String latStr = extractValue(firstObject, "\"lat\":\"", "\"");
                    String lonStr = extractValue(firstObject, "\"lon\":\"", "\"");
                    
                    // Si pas trouvé avec guillemets, essayer sans guillemets (format numérique)
                    if (latStr == null || lonStr == null) {
                        latStr = extractValue(firstObject, "\"lat\":", ",");
                        lonStr = extractValue(firstObject, "\"lon\":", ",");
                    }
                    
                    // Si toujours pas trouvé, essayer avec des espaces
                    if (latStr == null || lonStr == null) {
                        latStr = extractValue(firstObject, "\"lat\" : \"", "\"");
                        lonStr = extractValue(firstObject, "\"lon\" : \"", "\"");
                    }

                    if (latStr != null && lonStr != null) {
                        try {
                            // Nettoyer les espaces, guillemets et autres caractères
                            latStr = latStr.trim().replace("\"", "").replace("}", "").replace("]", "");
                            lonStr = lonStr.trim().replace("\"", "").replace("}", "").replace("]", "");
                            
                            Double latitude = Double.parseDouble(latStr);
                            Double longitude = Double.parseDouble(lonStr);

                            Map<String, Double> coordinates = new HashMap<>();
                            coordinates.put("latitude", latitude);
                            coordinates.put("longitude", longitude);

                            log.info("✅ Coordonnées récupérées: lat={}, lon={}", latitude, longitude);
                            return coordinates;
                        } catch (NumberFormatException e) {
                            log.error("❌ Erreur de parsing des coordonnées: latStr='{}', lonStr='{}', error={}", 
                                    latStr, lonStr, e.getMessage());
                        }
                    } else {
                        log.warn("⚠️ Coordonnées lat/lon non trouvées dans la réponse: {}", firstObject);
                    }
                }
            } else if (response != null && response.equals("[]")) {
                log.warn("⚠️ Aucun résultat trouvé pour l'adresse: {}", address);
            }

            log.warn("⚠️ Aucune coordonnée trouvée pour l'adresse: {}", address);
            return null;

        } catch (Exception e) {
            log.error("❌ Erreur lors de la géolocalisation pour l'adresse '{}': {}", address, e.getMessage());
            return null;
        }
    }

    /**
     * Méthode utilitaire pour extraire une valeur d'une chaîne JSON
     */
    private String extractValue(String json, String startMarker, String endMarker) {
        int startIndex = json.indexOf(startMarker);
        if (startIndex == -1) return null;
        startIndex += startMarker.length();
        int endIndex = json.indexOf(endMarker, startIndex);
        if (endIndex == -1) return null;
        return json.substring(startIndex, endIndex);
    }

    /**
     * Extrait le premier objet JSON d'un tableau JSON
     */
    private String extractFirstObject(String jsonArray) {
        if (jsonArray == null || !jsonArray.startsWith("[")) {
            return null;
        }
        
        int startIndex = jsonArray.indexOf("{");
        if (startIndex == -1) return null;
        
        int braceCount = 0;
        int endIndex = startIndex;
        
        for (int i = startIndex; i < jsonArray.length(); i++) {
            char c = jsonArray.charAt(i);
            if (c == '{') braceCount++;
            if (c == '}') braceCount--;
            if (braceCount == 0) {
                endIndex = i + 1;
                break;
            }
        }
        
        if (endIndex > startIndex) {
            return jsonArray.substring(startIndex, endIndex);
        }
        
        return null;
    }

    /**
     * Vérifie si une adresse est géolocalisable
     */
    public boolean isGeolocalizable(String address) {
        return address != null && !address.trim().isEmpty();
    }
}

