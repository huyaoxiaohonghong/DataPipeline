# =============================================================================
# DataPipeline Backend Dockerfile (Spring Boot 4.0 + JDK 25)
# =============================================================================

# Stage 1: Build the application using Maven and JDK 25
FROM maven:3.9.9-eclipse-temurin-25-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies (for caching layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy src and build the package
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Create the runtime image
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/data-pipeline-1.0.0-SNAPSHOT.jar app.jar

# Expose backend port
EXPOSE 8080

# Environment variables with defaults
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="--enable-preview"

# Run the application with preview features enabled (needed for Virtual Threads)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
