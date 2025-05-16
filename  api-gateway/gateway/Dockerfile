# Use an official Java 21 runtime as a parent image
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory in the container
WORKDIR /app

# Arguments for JAR file and port
ARG JAR_FILE=target/api-gateway-1.0.0-SNAPSHOT.jar
ARG APP_PORT=8080

# Copy the executable JAR from the build stage to the current directory in the container
COPY ${JAR_FILE} app.jar

# Expose the port the application runs on
EXPOSE ${APP_PORT}

# Command to run the application
# Add any JVM options here if needed, e.g., -Xmx512m -Xms256m
# Spring profiles can be activated using -Dspring.profiles.active=docker
ENTRYPOINT ["java", "-jar", "app.jar"]