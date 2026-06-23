# -- Request Service Dockerfile -------------------------------------------------
# Stage 1: Build the Maven artifact
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy all POM files first. This allows Docker to cache dependencies and avoid redownloading them if source code changes
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY request-service/pom.xml request-service/pom.xml

# Pre-fetch dependencies specifically for request-service to optimize subsequent builds
RUN mvn -pl request-service dependency:go-offline -B --no-transfer-progress 2>/dev/null || true

# Copy source code of the service
COPY request-service/src request-service/src

# Compile and package the service into a JAR file, skipping tests since they run in Jenkins
RUN mvn -pl request-service package -DskipTests -B --no-transfer-progress

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root system user and group for better container security (least privilege)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the built JAR from the builder stage, keeping the image size small
COPY --from=build /app/request-service/target/*.jar app.jar

# Expose Request Service port
EXPOSE 8083

# Periodically verify the request-service is healthy via Spring Boot Actuator
HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
  CMD wget -qO- http://localhost:8083/actuator/health | grep -q '"status":"UP"' || exit 1

# Start application using urandom as the secure random generator (improves Spring startup time)
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
