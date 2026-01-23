# ==============================================================================
# Dockerfile for Books Backend Service
# ==============================================================================
# Multi-stage build:
# 1. Build stage: Compiles the application using Maven
# 2. Runtime stage: Runs the application with a minimal JRE image
# 
# Design decision: Multi-stage build keeps the final image small by excluding
# build tools and source code from the runtime image.
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first (for dependency caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make wrapper executable
RUN chmod +x mvnw

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application (skip tests for faster builds in Docker)
RUN ./mvnw package -DskipTests -B

# ------------------------------------------------------------------------------
# Stage 2: Runtime
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

USER appuser

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
# JVM options for containerized environment
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", "app.jar"]

# ------------------------------------------------------------------------------
# Build and Run:
#   docker build -t books-backend-service .
#   docker run -p 8080:8080 \
#       -e BOOKS_DB_TYPE=mysql \
#       -e BOOKS_DB_URL=jdbc:mysql://host.docker.internal:3306/booksdb?user=root&password=root \
#       books-backend-service
# 
# Environment variables override application.properties:
#   BOOKS_DB_TYPE: mysql or mongo
#   BOOKS_DB_URL: connection string for the selected database
# ------------------------------------------------------------------------------
