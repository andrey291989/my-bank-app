# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy parent POM and modules
COPY pom.xml .
COPY front-ui/pom.xml front-ui/
COPY gateway/pom.xml gateway/
COPY accounts-service/pom.xml accounts-service/
COPY cash-service/pom.xml cash-service/
COPY transfer-service/pom.xml transfer-service/
COPY notifications-service/pom.xml notifications-service/

# Copy source code for notifications-service
COPY notifications-service/src notifications-service/src/

# Build only notifications-service module
RUN mvn clean package -pl notifications-service -am -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/notifications-service/target/notifications-service-*.jar app.jar

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]