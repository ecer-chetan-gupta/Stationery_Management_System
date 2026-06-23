# -- Eureka Server Dockerfile --------------------------------------------------
# Stage 1: Build with Maven + JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy parent POM first (for dependency resolution)
# By copying POMs first, we take advantage of Docker layer caching to skip fetching maven dependencies if they haven't changed
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY request-service/pom.xml request-service/pom.xml

# Download dependencies (layer caching)
RUN mvn -pl eureka-server dependency:go-offline -B --no-transfer-progress 2>/dev/null || true

# Copy source code of the service
COPY eureka-server/src eureka-server/src

# Build (skip tests in CI build; tests run in separate Jenkins stage)
RUN mvn -pl eureka-server package -DskipTests -B --no-transfer-progress

# -- Stage 2: Minimal JRE runtime ----------------------------------------------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Security: run as non-root (prevents directory traversal or privilege escalation exploits)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the packaged jar from the builder stage
COPY --from=build /app/eureka-server/target/*.jar app.jar

# Expose service registry default port
EXPOSE 8761

# Monitor Eureka server health check via standard HTTP check
HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
  CMD wget -qO- http://localhost:8761/actuator/health | grep -q '"status":"UP"' || exit 1

# Start container using faster entropy source (urandom)
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
