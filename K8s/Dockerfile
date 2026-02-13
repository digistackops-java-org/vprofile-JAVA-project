# Multi-stage Dockerfile for VProfile Application
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests in Docker build, run them separately)
RUN mvn clean package -DskipTests

# Stage 2: Create runtime image
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create app user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy WAR file from build stage
COPY --from=build /app/target/vprofile.war /app/vprofile.war

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check using the /health/live endpoint
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health/live || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/vprofile.war"]

# Optional: Add JVM options
# ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "/app/vprofile.war"]
