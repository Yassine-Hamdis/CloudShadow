# ─── Stage 1: Build ───────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml first (layer caching — dependencies won't re-download if unchanged)
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the JAR without running tests
RUN mvn clean package -DskipTests

# ─── Stage 2: Run ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN addgroup -S cloudshadow && adduser -S cloudshadow -G cloudshadow

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Give ownership to non-root user
RUN chown cloudshadow:cloudshadow app.jar

# Switch to non-root user
USER cloudshadow

# Expose backend port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]