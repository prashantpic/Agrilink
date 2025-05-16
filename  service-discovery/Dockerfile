# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Arguments for the JAR file
ARG JAR_FILE=target/*.jar

# Set the working directory in the container
WORKDIR /opt/app

# Copy the executable JAR file to the container
COPY ${JAR_FILE} app.jar

# Expose the port the application runs on (default Eureka port is 8761)
# This should match server.port in application.yml
EXPOSE 8761

# Define environment variables (optional, can be set at runtime)
# ENV SPRING_PROFILES_ACTIVE=docker
# ENV SPRING_CONFIG_IMPORT=optional:configserver:http://config-server:8888
# ENV JAVA_TOOL_OPTIONS="-javaagent:/path/to/apm-agent.jar" # If using an external APM agent

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]