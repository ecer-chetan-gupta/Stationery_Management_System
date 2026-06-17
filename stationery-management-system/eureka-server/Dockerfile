# ============================================================
# Multi-stage build for Eureka Server
# Stage 1: Build the JAR with Maven
# Stage 2: Run the JAR with a slim JRE image
# ============================================================

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the parent POM first (for dependency caching)
COPY ../pom.xml /parent-pom.xml

# Copy this module's POM and source
COPY pom.xml .
COPY src ./src

# Build, skip tests (tests run in CI pipeline separately)
RUN mvn clean package -DskipTests

# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8761

# Health check — wait up to 60s for Eureka to be ready
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD wget --quiet --tries=1 --spider http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
