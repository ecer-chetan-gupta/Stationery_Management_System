# ── Config Server Dockerfile ───────────────────────────────────────────────────
# In Docker, we switch to 'native' profile so config-server reads from
# classpath:/config/ (baked into the image). The git-based approach used
# in local dev doesn't translate to Docker containers easily.
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY request-service/pom.xml request-service/pom.xml

RUN mvn -pl config-server dependency:go-offline -B --no-transfer-progress 2>/dev/null || true

COPY config-server/src config-server/src

RUN mvn -pl config-server package -DskipTests -B --no-transfer-progress

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/config-server/target/*.jar app.jar

EXPOSE 8888

HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
  CMD wget -qO- http://localhost:8888/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
