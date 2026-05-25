# =============================================================================
# DataPipeline Backend Dockerfile (Spring Boot 4.0 + JDK 25)
# =============================================================================

# Stage 1: Build the application using Eclipse Temurin JDK 25 and Maven
FROM eclipse-temurin:25-jdk-alpine AS build
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
RUN apk add --no-cache maven
WORKDIR /app

# Set Maven memory limits to prevent host OOM kills during compilation on 4GB RAM hosts
ENV MAVEN_OPTS="-Xmx512m -XX:+UseSerialGC"

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
