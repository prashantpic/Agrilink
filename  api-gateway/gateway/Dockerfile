# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:21-jdk-jammy

# Set the working directory in the container
WORKDIR /app

# Arguments for user and group
ARG APP_USER=appuser
ARG APP_GROUP=appgroup
ARG APP_UID=1001
ARG APP_GID=1001

# Create a non-root user and group
RUN groupadd -g ${APP_GID} ${APP_GROUP} && \
    useradd -u ${APP_UID} -g ${APP_GROUP} -m -s /bin/bash ${APP_USER}

# Copy the fat JAR file into the container
# The JAR file is expected to be in target/ after a 'mvn package'
# Using a wildcard to match the versioned JAR file
COPY target/api-gateway-*.jar api-gateway.jar

# Make port 8080 available to the world outside this container
# This should match the server.port in application.yml
EXPOSE 8080

# Change ownership to the non-root user
RUN chown ${APP_USER}:${APP_GROUP} api-gateway.jar

# Switch to the non-root user
USER ${APP_USER}

# Define environment variables (can be overridden at runtime)
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE="docker"

# Run the JAR file
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/api-gateway.jar"]