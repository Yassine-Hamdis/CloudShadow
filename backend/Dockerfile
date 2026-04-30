# ─── Stage 1: Build ───────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml first (layer caching)
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source
COPY src ./src

# Build jar
RUN mvn clean package -DskipTests

# ─── Stage 2: Run ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

ENV TZ=Africa/Casablanca

WORKDIR /app

# Non-root user for security
RUN addgroup -S cloudshadow && \
    adduser -S cloudshadow -G cloudshadow

# Copy jar
COPY --from=builder /app/target/*.jar app.jar

# Ownership
RUN chown cloudshadow:cloudshadow app.jar

USER cloudshadow

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s \
  --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 \
  --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]