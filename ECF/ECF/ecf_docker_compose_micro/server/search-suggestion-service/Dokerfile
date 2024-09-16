# Dockerfile for search-suggestion-service
FROM openjdk:17-jdk-slim

# Define the working directory
WORKDIR /app

# Copy the built application jar (Assume you will build the jar file with Maven/Gradle)
COPY target/search-suggestion-service.jar app.jar

# Expose the port for the service
EXPOSE 8084

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
