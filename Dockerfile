# Multi-stage build for Spring Boot application
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S -g 1000 appgroup && adduser -S -u 1000 -G appgroup appuser

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/chat-application-*.jar chat-application.jar

USER appuser

# Expose port
EXPOSE 5000

# Run application
ENTRYPOINT ["java", "-jar", "chat-application.jar"]
