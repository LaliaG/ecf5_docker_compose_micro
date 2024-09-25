Pour l'ECF : Il faut faire en sorte que le projet sur git soit opérationnel dans des containers. Prenez le temps de bien comprendre le projet. Ensuite faire les dockerfile pour chacun des microservices et aussi pour les containers qui vont être utilisés pour la persistance. Enfin créer un docker-compose qui va vous permettre de faire fonctionner le tout ensemble. 

# ECF5 DOCKER COMPOSE MICRO 16/09/2024 LALIA SY

## Maquette
[documentation](./documentation/)


## App Client mysql-db server
[client](./ECF/ECF/ecf_docker_compose_micro/client/)
[mysql-db](./ECF/ECF/ecf_docker_compose_micro/mysql-db/)
[server](./ECF/ECF/ecf_docker_compose_micro/server/)

## Objectif de ce projet
Répondre au sujet du [ECF_5_16_09_2024](./ECF_5_16_09_2024.md/)

Je comprends la problématique que tu rencontres dans ton projet avec Docker Compose pour une application React et des microservices. Voici un point par point pour t’expliquer la situation et comment la résoudre efficacement.

### Contexte

Dans une application React, les **variables d’environnement** doivent être injectées **au moment du build**. Si elles ne sont pas présentes lors de la compilation, elles ne peuvent pas être modifiées après coup. C’est pourquoi, dans ton cas, les variables passées via Docker Compose ne fonctionnent pas après le build.

### Problème

Lorsque tu utilises un **Dockerfile** pour construire ton image React, il faut injecter les variables d’environnement **avant** ou **pendant le build**, car une fois l’application React construite (en production), les fichiers générés (HTML/CSS/JS) sont statiques et n'utilisent plus directement les variables d’environnement.

### Solutions pour gérer les variables d’environnement dans une application React avec Docker :

#### 1. **Utiliser les variables au moment du build dans le Dockerfile**

Dans ton `Dockerfile`, tu peux déclarer les **arguments** ou **variables d’environnement** au moment du build. Cela permet d'injecter les bonnes valeurs dans le build final.

**Exemple d'un Dockerfile avec les variables passées en tant qu'arguments :**

```Dockerfile
# Étape 1 : Builder l'application
FROM node:14 AS build-stage

# Déclaration des variables d'environnement via ARG
ARG REACT_APP_AUTHENTICATION_SERVICE_URL
ARG REACT_APP_COMMON_DATA_SERVICE_URL
ARG REACT_APP_SEARCH_SUGGESTION_SERVICE_URL

# Définit le répertoire de travail
WORKDIR /app

# Copie les fichiers package.json et package-lock.json et installe les dépendances
COPY package*.json ./
RUN npm install

# Copie le reste des fichiers de l'application
COPY . .

# Build de l'application avec les variables d'environnement injectées
RUN REACT_APP_AUTHENTICATION_SERVICE_URL=$REACT_APP_AUTHENTICATION_SERVICE_URL \
    REACT_APP_COMMON_DATA_SERVICE_URL=$REACT_APP_COMMON_DATA_SERVICE_URL \
    REACT_APP_SEARCH_SUGGESTION_SERVICE_URL=$REACT_APP_SEARCH_SUGGESTION_SERVICE_URL \
    npm run build

# Étape 2 : Configurer un serveur pour servir l'application
FROM nginx:alpine AS production-stage

# Copie les fichiers de build vers le serveur Nginx
COPY --from=build-stage /app/build /usr/share/nginx/html

# Expose le port
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

#### 2. **Utiliser un fichier `.env` pendant le build**

Si tu souhaites passer un fichier `.env` contenant tes variables d’environnement, tu peux le **copier** dans ton image pendant le build.

**Exemple d'un Dockerfile avec un fichier `.env` copié dans l'image :**

```Dockerfile
# Étape 1 : Builder l'application
FROM node:14 AS build-stage

# Copie du fichier .env
COPY .env ./

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

# Build de l'application en utilisant le fichier .env
RUN npm run build

# Étape 2 : Configurer un serveur pour servir l'application
FROM nginx:alpine AS production-stage

COPY --from=build-stage /app/build /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

Dans ce cas, ton fichier `.env` serait copié pendant le build, et les variables qu’il contient seront lues par React lors de la compilation.

#### 3. **Utiliser un script d’exécution pour injecter des variables après le build**

Une autre méthode consiste à **injecter dynamiquement des variables d’environnement** après le build en utilisant un script d’entrée (entrypoint). Cependant, cela nécessite de servir les fichiers statiques d’une manière plus flexible.

Tu pourrais modifier les fichiers de configuration (par exemple, `index.html` ou des fichiers JS) **au démarrage du container** avec des variables d’environnement. C’est plus complexe et généralement moins pratique que d’injecter les variables lors du build.

#### 4. **Utilisation d’un proxy ou d’un reverse proxy**

Si ton application React communique avec des services backend, tu peux aussi utiliser un **reverse proxy** comme Nginx pour **rediriger les appels API** vers les bons services en fonction de leur URL ou du port. Cela évite de devoir injecter ces variables directement dans React, et tu te concentres sur le backend.

---

### Docker Compose : Définir les variables d’environnement dans `docker-compose.yml`

Dans ton fichier `docker-compose.yml`, tu peux passer les **variables d'environnement** au moment du build en utilisant l'instruction `build.args` :

```yaml
version: '3'
services:
  react-app:
    build:
      context: .
      dockerfile: Dockerfile
      args:
        REACT_APP_AUTHENTICATION_SERVICE_URL: "http://localhost:7000"
        REACT_APP_COMMON_DATA_SERVICE_URL: "http://localhost:9000"
        REACT_APP_SEARCH_SUGGESTION_SERVICE_URL: "http://localhost:10000"
    ports:
      - "3000:80"
```

### Conclusion

Pour résoudre ton problème, je te recommande d’injecter les variables d’environnement **pendant le build** de ton application React, en utilisant soit les **arguments dans le Dockerfile**, soit un **fichier `.env` copié** avant le build. Cela te permettra de passer correctement les variables nécessaires pour la configuration de tes services backend, tout en évitant les limitations des variables d’environnement après le build.



