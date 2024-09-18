### **Deployment Documentation**

---

#### **1. Prerequisites**
Before starting, ensure you have the following installed:
- **Docker**: [Docker Installation Guide](https://docs.docker.com/get-docker/)
- **Docker Compose**: [Docker Compose Installation Guide](https://docs.docker.com/compose/install/)

You will also need:
- The `.env` file containing necessary environment variables for configuring services.
- The source code of the application (available through a Git repository).

---

#### **2. Deployment Steps**

##### **Step 1: Clone the project**
Start by cloning the project from the Git repository:

```bash
git clone <repository-url>
```

Replace `<repository-url>` with the actual URL of the Git repository. Then navigate to the project folder:

```bash
cd <cloned-folder-name>
```

---

##### **Step 2: Set up environment variables**
Ensure that you have an `.env` file in the root directory of the project with the correct environment variables (e.g., MySQL password, service ports, etc.). If the `.env` file is missing, create one with the required variables.

Example `.env` file:

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
# Other variables ...
```

---

##### **Step 3: Build Docker images**
In the project directory, run the following command to build Docker images for each microservice and for the React UI:

```bash
docker-compose build
```

---

##### **Step 4: Start the application**
After building the images, start the application using Docker Compose. This will launch all the Docker containers defined in the `docker-compose.yml` file:

```bash
docker-compose up
```

To run the services in detached mode (in the background), use:

```bash
docker-compose up -d
```

---

##### **Step 5: Access the user interface**
Once the services are running, you can access the user interface through your browser at the following address:

```
http://localhost:${REACT_APP_PORT}
```

Replace `${REACT_APP_PORT}` with the port defined in your `.env` file (default is `3000`).

---

##### **Step 6: Test microservice communication**
The React UI consumes APIs provided by the microservices. Here’s how to test the communication between the React interface and the services:

- **Authentication**: Register a new user or log in via the UI.
- **Common Data**: Check displayed data such as products or categories.
- **Search Suggestion**: Test the search functionality to ensure the **search suggestion service** returns results.

You can also directly test each microservice using tools like **Postman** or by running `curl` commands. For example, to test the **search-suggestion-service**:

```bash
curl http://localhost:${SEARCH_SUGGESTION_SERVICE_PORT}/api/v1/search?q=product
```

---

##### **Step 7: Check service logs**
To debug or monitor the status of services, you can view the logs of the containers using:

```bash
docker-compose logs -f
```

Or for a specific service:

```bash
docker-compose logs <service-name>
```

---

##### **Step 8: Stop the containers**
To stop all the services and clean up the containers, use:

```bash
docker-compose down
```

This will stop all containers and clean up the environment properly.

---

#### **3. Deliverables**

- **Dockerfiles for each microservice and for the React UI**: Each microservice should have a `Dockerfile` that defines how to build the Docker image for that service.

  Example for a Spring Boot microservice (e.g., `search-suggestion-service`):
  ```dockerfile
  FROM openjdk:17-jdk-slim

  WORKDIR /app

  COPY target/search-suggestion-service.jar app.jar

  EXPOSE 8084

  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```

  Example for the React UI:
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

- **`docker-compose.yml` file**: This file orchestrates all the services in the application, defines dependencies, ports, and necessary environment variables for each service.

  Example:
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
    # Other services...

  networks:
    ecommerce-network:

  volumes:
    mysql-data:
  ```

---

#### **4. Testing Process**

Once the application is up and running, you can test the following:

1. **Authentication**: Register a new user or log in via the user interface.
2. **Product and Category Management**: Check product and category data.
3. **Search Functionality**: Use the search bar to test the search suggestion service.
   
   If the search works correctly, it confirms that the communication with the search service is properly established.

Make sure to observe the logs of the services to confirm proper functioning and troubleshoot any errors.


## Remark

No, you're not restricted to using only the **`maven:3.9.6-eclipse-temurin-11-alpine`** image. You can choose a different Java or Maven base image depending on your needs. Below are some alternative Java 11 images you can use, depending on your preferences for image size or specific distribution.

### Java 11 Image Alternatives:

1. **`openjdk:11-jdk-slim`**  
   This is a lightweight version of OpenJDK 11 based on a minimal Linux distribution.
   
   ```dockerfile
   FROM openjdk:11-jdk-slim
   ```

   - **Pros**: Smaller image, faster to download and use.
   - **Use Case**: If you don't need Maven in the image and just want to run a Java 11 application.

2. **`openjdk:11-alpine`**  
   This version of OpenJDK 11 uses **Alpine Linux**, an even more minimal Linux distribution than `slim`.

   ```dockerfile
   FROM openjdk:11-alpine
   ```

   - **Pros**: Extremely lightweight due to Alpine.
   - **Cons**: Some compatibility issues might arise due to Alpine's minimalist environment.

3. **`adoptopenjdk:11-jdk-hotspot`**  
   If you prefer using AdoptOpenJDK (now rebranded as Eclipse Temurin), this is another popular distribution of OpenJDK with the HotSpot JVM.

   ```dockerfile
   FROM adoptopenjdk:11-jdk-hotspot
   ```

   - **Pros**: Reliable and well-supported.
   - **Cons**: Slightly larger image size compared to `slim` or `alpine`.

4. **`azul/zulu-openjdk:11`**  
   Zulu OpenJDK is another distribution, commonly used in enterprise environments.

   ```dockerfile
   FROM azul/zulu-openjdk:11
   ```

   - **Pros**: Well-maintained and with optimizations for certain architectures.

### If You Need Maven for Building:
If you need Maven in your Dockerfile to compile your application, you can either choose an image with Maven and JDK pre-installed or install Maven manually.

1. **`maven:3.9.6-openjdk-11`**  
   Use this combined image with Maven and OpenJDK 11 to build your application.

   ```dockerfile
   FROM maven:3.9.6-openjdk-11
   ```

2. **Manual Maven Installation**:  

   Alternatively, you can install Maven manually in a JDK-only image:

   ```dockerfile
   FROM openjdk:11-jdk-slim

   # Manually install Maven
   RUN apt-get update && apt-get install -y maven
   ```

### Example of an Alternative with OpenJDK and Maven:

If you want to build a Docker image for your microservice without Alpine or pre-installed Maven, you can use the following setup:

```dockerfile
# Step 1: Build with OpenJDK + Maven
FROM maven:3.9.6-openjdk-11 AS builder

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Use a lightweight image for running the application
FROM openjdk:11-jdk-slim

WORKDIR /app
COPY --from=builder /app/target/<microservice-name>.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

You have multiple options for Java 11 images based on your requirements for size, compatibility, and environment.