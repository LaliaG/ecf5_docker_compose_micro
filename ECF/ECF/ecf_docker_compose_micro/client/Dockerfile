# Étape 1 : Builder l'application
FROM node:14 
#AS build-stage

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
ENV REACT_APP_AUTHENTICATION_SERVICE_URL=${REACT_APP_AUTHENTICATION_SERVICE_URL} 
ENV REACT_APP_COMMON_DATA_SERVICE_URL=${REACT_APP_COMMON_DATA_SERVICE_URL} 
ENV REACT_APP_SEARCH_SUGGESTION_SERVICE_URL=${REACT_APP_SEARCH_SUGGESTION_SERVICE_URL} 
RUN npm run build

EXPOSE 3000

CMD [ "npm", "run", "start" ]

# Étape 2 : Configurer un serveur pour servir l'application
# FROM nginx:alpine AS production-stage

# # Copie les fichiers de build vers le serveur Nginx
# COPY --from=build-stage /app/build /usr/share/nginx/html

# # Expose le port
# EXPOSE 3000

# CMD ["nginx", "-g", "daemon off;"]


# Dockerfile for React UI
# Exemple avec multistaging : node + nginx
# Etape 1 contruire l'application avec node
#FROM node AS build

# Define the working directory
#WORKDIR /usr/src/app

# Copy package.json and install dependencies
#COPY ./client/package.json ./
# COPY package-lock.json .

#RUN yarn install

# Copy the rest of the application files
#COPY . .

# Build the React application
#RUN yarn run build


# Etape 2 : deployer l'application build sur un serveur nginx
#FROM nginx:alpine

#RUN rm -rf /usr/share/nginx/html/*

# Copier le build de mon application react a partir de l'etape precedente dans le repertoire nginx 
#COPY --from=build /usr/src/app/build /usr/share/nginx/html

# Expose the port used by React (from .env: 3000)
#EXPOSE 3000

# Start the application using serve (or any other server you prefer)
#CMD ["npx", "serve", "-s", "build", "-l", "3000"]
#CMD ["nginx","-g","daemon off;"]









