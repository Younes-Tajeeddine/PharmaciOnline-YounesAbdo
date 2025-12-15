# 🧪 Test de la Géolocalisation

## Vérification que tout fonctionne

### 1. Vérifier les dépendances

Assurez-vous que `spring-boot-starter-webflux` est dans le `pom.xml` :
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

### 2. Vérifier la configuration

Dans `application.properties` :
```properties
geolocation.api.url=https://nominatim.openstreetmap.org/search
geolocation.api.timeout=5000
```

### 3. Tester la création d'une livraison

1. **Créer un client avec une adresse complète** :
   - Exemple : "123 Rue de la République, Paris, France"
   - Ou : "Avenue Mohammed V, Casablanca, Maroc"

2. **Créer une commande** pour ce client

3. **La livraison sera créée automatiquement** avec géolocalisation

### 4. Vérifier les logs

Vous devriez voir dans les logs :
```
🌍 Tentative de géolocalisation pour l'adresse: ...
✅ Coordonnées récupérées: lat=..., lon=...
📍 Coordonnées GPS: ..., ...
```

### 5. Vérifier dans la base de données

Les champs `latitude` et `longitude` doivent être remplis dans la table `Livraison`.

### 6. Tester l'endpoint de coordonnées

```bash
GET http://localhost:8084/livraisons/{id}/coordinates
```

Réponse attendue :
```json
{
  "latitude": 48.8566,
  "longitude": 2.3522
}
```

## Exemples d'adresses pour tester

- ✅ "Paris, France"
- ✅ "Casablanca, Maroc"
- ✅ "123 Rue de la République, 75001 Paris, France"
- ✅ "Avenue Mohammed V, Casablanca, Maroc"

## Dépannage

### Si la géolocalisation ne fonctionne pas :

1. **Vérifier la connexion Internet** : L'API Nominatim nécessite une connexion
2. **Vérifier les logs** : Chercher les erreurs dans les logs
3. **Tester l'API directement** :
   ```
   https://nominatim.openstreetmap.org/search?q=Paris&format=json&limit=1
   ```
4. **Vérifier que WebClient est configuré** : Le bean `WebClientConfig` doit être chargé

### Erreurs courantes :

- ❌ **Timeout** : Augmenter `geolocation.api.timeout` dans `application.properties`
- ❌ **Adresse non trouvée** : Utiliser une adresse plus précise
- ❌ **Erreur de parsing** : Vérifier les logs pour voir la réponse de l'API

## Notes importantes

- ⚠️ L'API Nominatim a une limite de **1 requête par seconde**
- ✅ La livraison est créée même si la géolocalisation échoue
- ✅ Les coordonnées sont optionnelles (peuvent être null)

