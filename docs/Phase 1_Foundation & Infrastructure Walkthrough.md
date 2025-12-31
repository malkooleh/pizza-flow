# Phase 1: Foundation & Infrastructure Walkthrough

## 1. Changes Made
- **Directory Structure:** Created standard microservices layout.
- **Root & Module POMs:** Configured Maven dependencies for Spring Boot 3.4.1.
- **Infrastructure:** Created `docker-compose.yaml` with Postgres, Mongo, Redis, Kafka, Keycloak.
- **Services Implemented:**
    - **Discovery Service (Eureka):** Port 8761.
    - **Config Service:** Port 8888, serving Native/Git config.
    - **API Gateway:** Port 8080, with Eureka client and OAuth2 setup.
- **Common Libs:** Created scaffolding for `common-dto` and `common-security`.

## 2. How to Run

### Step 1: Start Infrastructure
```bash
cd infrastructure/docker
docker-compose up -d
```
*Wait for ~30-60 seconds for services to become healthy.*

### Step 2: Build Project
```bash
# From project root
./mvnw clean install
```

### Step 3: Run Core Services
You can run these in separate terminal windows or your IDE:

**Discovery Service:**
```bash
./mvnw spring-boot:run -pl services/discovery-service
```

**Config Service:**
```bash
./mvnw spring-boot:run -pl services/config-service
```

**API Gateway:**
```bash
./mvnw spring-boot:run -pl services/api-gateway
```

## 3. Verification Steps

1. **Eureka Dashboard:**
    - Open [http://localhost:8761](http://localhost:8761)
    - Verify `API-GATEWAY` and `CONFIG-SERVICE` appear (Gateway might take a moment).

2. **Config Server:**
    - Check [http://localhost:8888/api-gateway/default](http://localhost:8888/api-gateway/default)
    - Should return JSON config sourced from `config-repo`.

3. **Keycloak:**
    - Admin Console: [http://localhost:8081](http://localhost:8081)
    - User/Pass: `admin`/`admin` (as defined in compose).