### **Documentation du Processus de Déploiement**

---

#### **1. Pré-requis**
Avant de commencer, assurez-vous d'avoir installé :
- **Docker** : [Documentation d'installation de Docker](https://docs.docker.com/get-docker/)
- **Docker Compose** : [Documentation d'installation de Docker Compose](https://docs.docker.com/compose/install/)

Vous aurez également besoin de :
- Le fichier `.env` contenant les variables d'environnement nécessaires pour configurer les services.
- Le code source de l'application (disponible via un dépôt Git).

---

#### **2. Étapes de Déploiement**

##### **Étape 1 : Cloner le projet**
Commencez par cloner le projet depuis le dépôt Git :

```bash
git clone <URL-du-dépôt>
```
Remplacez `<URL-du-dépôt>` par l'URL spécifique du dépôt Git. Puis, accédez au dossier du projet :

```bash
cd <nom-du-dossier-cloné>
```

---

##### **Étape 2 : Configuration des variables d'environnement**
Vérifiez que vous avez un fichier `.env` dans le répertoire du projet avec les bonnes variables d'environnement (par exemple, le mot de passe MySQL, les ports des services, etc.). Si le fichier `.env` n'est pas présent, créez-le avec les variables nécessaires.

Exemple de contenu du fichier `.env` :

```bash
MYSQL_ROOT_PASSWORD=mypass
MYSQL_DATABASE=ecommerce_app_database
MYSQL_USER=mysqluser
MYSQL_PASSWORD=mypass

DB_PORT=3306
DB_SCHEMA=ecommerce_app_database
DB_USER=mysqluser
DB_PASS=mypass

REACT_APP_PORT=3000
# Autres variables ...
```

---

##### **Étape 3 : Construire les images Docker**
Dans le répertoire du projet, exécutez la commande suivante pour construire les images Docker pour chaque microservice et pour l'interface utilisateur React :

```bash
docker-compose build
Remarque:
Pourquoi utiliser une multi-stage build ?
La méthode de build multi-stage permet de :

Construire l'application dans une image contenant Maven et JDK.
Copier uniquement l'application compilée (le fichier JAR) dans une deuxième image qui est plus légère (par exemple, une image avec seulement OpenJDK sans Maven).
Cela permet de réduire la taille de l'image Docker finale en n'incluant que les éléments nécessaires à l'exécution (sans les outils de compilation).
```

---

##### **Étape 4 : Démarrer l'application**
Une fois les images construites, démarrez l'application avec Docker Compose. Cette commande va lancer tous les conteneurs Docker définis dans le fichier `docker-compose.yml` :

```bash
docker-compose up
```

Si vous souhaitez démarrer les services en arrière-plan (mode détaché), utilisez :

```bash
docker-compose up -d
```

---

##### **Étape 5 : Accéder à l'interface utilisateur**
Une fois les services démarrés, vous pouvez accéder à l'interface utilisateur via votre navigateur à l'adresse suivante :

```
http://localhost:${REACT_APP_PORT}
```
Remplacez `${REACT_APP_PORT}` par le port défini dans votre fichier `.env` (par défaut, c'est `3000`).

---

##### **Étape 6 : Tester la communication avec les microservices**
L'interface utilisateur consomme les APIs fournies par les microservices. Voici comment tester la communication entre l'interface React et les services :

- **Authentification** : Créez un compte ou connectez-vous via l'interface.
- **Common Data** : Consultez les données affichées, telles que les produits ou catégories.
- **Search Suggestion** : Testez la fonctionnalité de recherche pour vérifier que le microservice de **suggestion de recherche** renvoie des résultats.

Vous pouvez également tester directement chaque microservice avec des outils comme **Postman** ou via des commandes `curl`. Par exemple, pour tester le **search-suggestion-service** :

```bash
curl http://localhost:${SEARCH_SUGGESTION_SERVICE_PORT}/api/v1/search?q=product
```

---

##### **Étape 7 : Consulter les logs des services**
Pour déboguer ou vérifier le bon fonctionnement des services, consultez les logs des conteneurs avec :

```bash
docker-compose logs -f
```
Ou spécifiquement pour un service :

```bash
docker-compose logs <nom-du-service>
```

---

##### **Étape 8 : Arrêter les conteneurs**
Pour arrêter tous les services et démonter les conteneurs, exécutez :

```bash
docker-compose down
```

Cela va arrêter tous les conteneurs et les nettoyer correctement.

---

#### **3. Livrables**

- **Dockerfiles pour chaque microservice et pour l'interface utilisateur** : Chaque microservice doit avoir un fichier `Dockerfile` qui définit comment construire l'image Docker pour ce service.
  
  Exemple pour un microservice Spring Boot (comme `search-suggestion-service`) :
  ```dockerfile
  FROM openjdk:17-jdk-slim

  WORKDIR /app

  COPY target/search-suggestion-service.jar app.jar

  EXPOSE 8084

  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```

  Exemple pour l'interface React :
  ```dockerfile
  FROM node:18-alpine

  WORKDIR /app

  COPY package*.json ./
  RUN npm install

  COPY . .

  RUN npm run build

  EXPOSE 3000

  CMD ["npx", "serve", "-s", "build", "-l", "3000"]
  ```

- **Fichier `docker-compose.yml`** : Ce fichier doit orchestrer tous les services de l'application, définir les dépendances, les ports, et les variables d'environnement nécessaires pour chaque service.
  
  Exemple :
  ```yaml
  version: '3.8'

  services:
    mysql-db:
      image: mysql:8.0
      environment:
        - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
        - MYSQL_DATABASE=${MYSQL_DATABASE}
        - MYSQL_USER=${MYSQL_USER}
        - MYSQL_PASSWORD=${MYSQL_PASSWORD}
      ports:
        - "${DB_PORT}:3306"
      volumes:
        - mysql-data:/var/lib/mysql
      networks:
        - ecommerce-network

    redis-cache:
      image: redis:alpine
      ports:
        - "${REDIS_PORT}:6379"
      networks:
        - ecommerce-network

    common-data-service:
      build: ./server/common-data-service
      environment:
        - SPRING_PROFILES_ACTIVE=prod
      ports:
        - "${COMMON_DATA_SERVICE_PORT}:8080"
      depends_on:
        - mysql-db
        - redis-cache
      networks:
        - ecommerce-network
    # Autres services...

  networks:
    ecommerce-network:

  volumes:
    mysql-data:
  ```

---

#### **4. Processus de Test**

Une fois l'application démarrée, vous pouvez tester :

1. **L'authentification** : Créez un compte ou connectez-vous via l'interface utilisateur.
2. **Gestion des produits et des catégories** : Consultez les données des produits, catégories, etc.
3. **Recherche** : Utilisez la barre de recherche pour tester le microservice de suggestion de recherche.
  
   Si la recherche fonctionne correctement, cela confirme que la communication avec le microservice de recherche est bien établie.

N'oubliez pas d'observer les logs des services pour vérifier leur bon fonctionnement et dépister d'éventuelles erreurs.


## Remarque

Non, vous n'êtes pas obligé d'utiliser uniquement l'image **`maven:3.9.6-eclipse-temurin-11-alpine`**. Vous pouvez choisir une autre image de Java ou de Maven en fonction de vos besoins. Voici quelques alternatives pour Java 11, selon vos préférences en matière de taille d'image ou de distribution spécifique.

### Alternatives pour Java 11 :

1. **`openjdk:11-jdk-slim`**  
   Cette image est une version légère d'OpenJDK 11, basée sur une distribution Linux minimale.
   
   ```dockerfile
   FROM openjdk:11-jdk-slim
   ```

   - Avantages : Image légère, plus rapide à télécharger et à utiliser.
   - Utilisation : Si vous n'avez pas besoin de Maven dans votre image et que vous souhaitez juste exécuter une application Java 11.

2. **`openjdk:11-alpine`**  
   Version OpenJDK 11 utilisant **Alpine Linux**, une distribution encore plus légère que **slim**.

   ```dockerfile
   FROM openjdk:11-alpine
   ```

   - Avantages : Image extrêmement légère grâce à Alpine.
   - Inconvénients : Certains problèmes de compatibilité peuvent survenir en raison de l'environnement minimaliste d'Alpine.

3. **`adoptopenjdk:11-jdk-hotspot`**  
   Si vous préférez utiliser AdoptOpenJDK (maintenant devenu Eclipse Temurin), une autre distribution populaire d'OpenJDK avec HotSpot.

   ```dockerfile
   FROM adoptopenjdk:11-jdk-hotspot
   ```

   - Avantages : Image robuste et bien supportée.
   - Inconvénients : Taille légèrement plus grande que `slim` ou `alpine`.

4. **`azul/zulu-openjdk:11`**  
   Zulu OpenJDK est une autre distribution d'OpenJDK, souvent utilisée en entreprise.

   ```dockerfile
   FROM azul/zulu-openjdk:11
   ```

   - Avantages : Une distribution d'OpenJDK bien maintenue et avec des optimisations pour certaines architectures.

### Si vous avez besoin de Maven pour construire votre projet :
Si vous devez utiliser Maven dans le Dockerfile pour compiler votre application, vous pouvez choisir une image avec Maven + JDK pré-installé ou installer Maven manuellement dans votre Dockerfile.

1. **`maven:3.9.6-openjdk-11`**  
   Utilisez une image combinée avec Maven et OpenJDK 11 pour la construction.

   ```dockerfile
   FROM maven:3.9.6-openjdk-11
   ```

2. **Installer Maven manuellement :**

   Si vous préférez, vous pouvez installer Maven dans une image JDK classique :

   ```dockerfile
   FROM openjdk:11-jdk-slim

   # Installer Maven manuellement
   RUN apt-get update && apt-get install -y maven
   ```

### Exemple d'alternative avec OpenJDK et Maven :

Si vous voulez construire une image Docker pour votre microservice sans Alpine ou Maven intégré, vous pouvez utiliser :

```dockerfile
# Étape 1 : Construction avec une image OpenJDK + Maven
FROM maven:3.9.6-openjdk-11 AS builder

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Étape 2 : Utilisation d'une image légère pour exécution
FROM openjdk:11-jdk-slim

WORKDIR /app
COPY --from=builder /app/target/<microservice-name>.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Vous avez donc plusieurs options d'images pour utiliser Java 11 selon vos exigences en termes de taille, de compatibilité et d'environnement.