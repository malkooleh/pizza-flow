# Build Stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy Parent and Common Libs
COPY pom.xml .
COPY common-libs common-libs/
COPY services/pom.xml services/

# Copy Service Source
COPY services/inventory-service services/inventory-service/

# Build Service
RUN mvn clean package -DskipTests -pl services/inventory-service -am

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Extract Jar
COPY --from=builder /app/services/inventory-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
