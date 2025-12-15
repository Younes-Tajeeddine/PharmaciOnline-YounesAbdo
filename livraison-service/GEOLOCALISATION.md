# 🌍 Service de Géolocalisation - Livraison Service

## Vue d'ensemble

Le service de livraison intègre maintenant un **service de géolocalisation** utilisant **WebClient** pour récupérer les coordonnées GPS (latitude, longitude) des adresses de livraison.

## Fonctionnalités

✅ **Géolocalisation automatique** lors de la création d'une livraison  
✅ **Stockage des coordonnées GPS** dans la base de données  
✅ **API gratuite** (OpenStreetMap Nominatim) - sans clé API requise  
✅ **Gestion d'erreurs** - La création de livraison ne échoue pas si la géolocalisation échoue  
✅ **Endpoint dédié** pour récupérer les coordonnées d'une livraison

## Architecture

### Composants

1. **GeolocationService** : Service de géolocalisation utilisant WebClient
2. **WebClientConfig** : Configuration du WebClient
3. **Livraison Entity** : Champs `latitude` et `longitude` ajoutés
4. **LivraisonService** : Intégration de la géolocalisation dans la création de livraison

### API Utilisée

**OpenStreetMap Nominatim API**
- URL : `https://nominatim.openstreetmap.org/search`
- Gratuite, sans clé API
- Rate limit : 1 requête par seconde (respectée automatiquement)
- Format de réponse : JSON

## Configuration

### application.properties

```properties
# Configuration de l'API de géolocalisation
geolocation.api.url=https://nominatim.openstreetmap.org/search
geolocation.api.timeout=5000
```

### Dépendances

Ajout de `spring-boot-starter-webflux` dans `pom.xml` pour WebClient.

## Utilisation

### Création Automatique

Lors de la création d'une livraison, le système :
1. Récupère l'adresse du client
2. Appelle l'API de géolocalisation avec WebClient
3. Parse la réponse JSON pour extraire latitude et longitude
4. Sauvegarde les coordonnées dans la base de données

### Exemple de Code

```java
// Dans LivraisonService.createLivraison()
Map<String, Double> coordinates = geolocationService.getCoordinates(adresse);
if (coordinates != null) {
    latitude = coordinates.get("latitude");
    longitude = coordinates.get("longitude");
}
```

## Endpoints API

### GET /livraisons/{id}/coordinates

Récupère les coordonnées GPS d'une livraison.

**Réponse** :
```json
{
  "latitude": 48.8566,
  "longitude": 2.3522
}
```

## Format de Données

### Entité Livraison

```java
@Entity
public class Livraison {
    // ... autres champs
    private Double latitude;   // Coordonnée GPS latitude
    private Double longitude;  // Coordonnée GPS longitude
}
```

## Gestion d'Erreurs

- ✅ Si l'adresse est vide → Pas de géolocalisation
- ✅ Si l'API ne répond pas → Log d'erreur, livraison créée sans coordonnées
- ✅ Si l'adresse n'est pas trouvée → Log d'avertissement, livraison créée sans coordonnées
- ✅ Timeout configuré (5 secondes par défaut)

## Logs

Le service génère des logs détaillés :
- `🌍 Tentative de géolocalisation pour l'adresse: ...`
- `✅ Coordonnées récupérées: lat=..., lon=...`
- `⚠️ Aucune coordonnée trouvée pour l'adresse: ...`
- `❌ Erreur lors de la géolocalisation: ...`

## Exemple d'Utilisation

### Création d'une Livraison

```bash
POST /livraisons/{commandeId}
```

Le système :
1. Crée la livraison
2. Récupère l'adresse du client
3. Appelle l'API de géolocalisation
4. Sauvegarde les coordonnées (si disponibles)

### Récupération des Coordonnées

```bash
GET /livraisons/1/coordinates
```

Retourne les coordonnées GPS de la livraison #1.

## Améliorations Possibles

1. **Cache** : Mettre en cache les coordonnées pour éviter les appels répétés
2. **Alternative API** : Support pour d'autres APIs (Google Maps, Mapbox)
3. **Géocodage inverse** : Convertir coordonnées → adresse
4. **Calcul de distance** : Calculer la distance entre deux points
5. **Optimisation de route** : Trouver le meilleur itinéraire

## Notes Techniques

- **WebClient** : Client réactif non-bloquant de Spring WebFlux
- **Timeout** : 5 secondes par défaut (configurable)
- **User-Agent** : Requis par Nominatim API
- **Format JSON** : Parsing manuel pour éviter les dépendances supplémentaires

