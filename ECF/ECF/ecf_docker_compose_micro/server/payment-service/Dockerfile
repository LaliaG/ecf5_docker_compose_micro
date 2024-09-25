# Dockerfile for payment-service
# Step 1: Building the app
FROM  maven:3.9.6-eclipse-temurin-11-alpine AS builder

# Define the working directory for Maven
WORKDIR /app

# Copies pom.xml files and source files to download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src

# Compile the application by skipping the tests
RUN mvn package -DskipTests

#Step 2: Runtime Image
FROM openjdk:11-jdk-slim

# Defines the working directory for execution
WORKDIR /app

# Copy the jar from the build step (Assume you will build the jar file with Maven/Gradle, in the pom file Maven is used) 
COPY --from=builder /app/target/payment-service.jar .

# Expose the port (use 9050 as specified in .env)
EXPOSE 9050

# Run the Spring Boot application
ENTRYPOINT ["java","-jar","payment-service-0.0.1.jar"]