# Dockerfile for common-data-service
FROM openjdk:17-jdk-slim

# Define the working directory
WORKDIR /app

# Copy the built application jar (Assume you will build the jar file with Maven/Gradle)
COPY target/common-data-service.jar app.jar

# Expose the port for the service
EXPOSE 8081

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
