# Phase 4.3: Containerization & Helm Walkthrough

This phase focused on "Day 2 Operations" preparation by standardizing how PizzaFlow services are built and deployed to Kubernetes.

## 1. Docker Optimization 📦

We moved from manual execution to containerized artifacts using multi-stage builds.

### Key Strategy
- **Base Image**: `eclipse-temurin:21-jre-alpine` (Minimal footprint, secure)
- **Pattern**: Multi-stage build
    1.  **Builder**: `maven:3.9-eclipse-temurin-21` - Compiles code and builds JAR.
    2.  **Runtime**: `eclipse-temurin:21-jre-alpine` - Runs the application.
- **Security**: Runs as non-root user `spring`.

### Example `Dockerfile`
```dockerfile
# Build Stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -pl services/order-service -am

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=builder /app/services/order-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 2. Helm Chart Architecture ☸️

We implemented a **Library Chart Pattern** to reduce boilerplate and ensure consistency across all 10 microservices.

### `infrastructure/helm/common` (Library Chart)
Defines the structure for **all** services. Any change here propagates to every service.
- `deployment.yaml`: Standard K8s Deployment with readiness/liveness probes.
- `service.yaml`: Standard ClusterIP/LoadBalancer service definition.
- `_helpers.tpl`: Common label generation logic.

### `infrastructure/helm/services/*` (Application Charts)
Lightweight wrappers that inherit from `common`.
- **Chart.yaml**: Declares dependency on `common`.
- **values.yaml**: Service-specific overrides (Ports, Env Vars, Resources).

### Example: Booking Service `values.yaml`
```yaml
image:
  repository: pizzaflow/booking-service
  tag: latest

service:
  port: 8085

env:
  SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres:5432/booking_db"
  SPRING_PROFILES_ACTIVE: "prod"
```

## 3. Deployment Guide 🚀

### Build Images
```bash
docker build -f services/order-service/Dockerfile -t pizzaflow/order-service:latest .
```

### Deploy to K8s (Local/Dev)
```bash
# Install Common Library (Local Dev)
# Not needed to 'install' a library chart, just reference it.

# Deploy Order Service
helm install order-service infrastructure/helm/services/order-service
```

## 4. Next Steps
- **Phase 4.4**: Implement CI/CD pipelines to build these Docker images automatically.
