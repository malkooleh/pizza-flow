# Build Stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy Parent and Common Libs
COPY pom.xml .
COPY common-libs common-libs/
COPY services/pom.xml services/

# Copy Service Source
COPY services/payment-service services/payment-service/

# Build Service
RUN mvn clean package -DskipTests -pl services/payment-service -am

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Extract Jar
COPY --from=builder /app/services/payment-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
