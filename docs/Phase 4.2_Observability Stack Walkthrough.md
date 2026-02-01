# Phase 4.2: Observability Stack Walkthrough

This phase focused on implementing a comprehensive observability stack for the PizzaFlow platform, ensuring full visibility into system health, performance, and user journeys using **ELK Stack**, **Prometheus**, **Grafana**, and **Micrometer Tracing**.

## 1. Metrics (Prometheus & Grafana)

We integrated **Micrometer** to collect and expose JVM and business metrics.

### 📊 Key Implementations:
- **Prometheus Scaping**: Every service now exposes metrics at `/actuator/prometheus`.
- **Infrastructure**: Prometheus and Grafana are provisioned via `docker-compose.yaml`.
- **Custom Metrics**:
    - `pizzaflow.orders.created`: Counter for total orders placed.
    - `pizzaflow.payments.result`: Counter for payment outcomes (`status=approved/declined`).
- **Dashboards**: Grafana is pre-configured to visualize JVM health, thread pools, and business KPIs.

## 2. Distributed Tracing (OpenTelemetry)

We implemented distributed tracing to track requests as they flow through multiple microservices and Kafka topics.

### 🕵️‍♂️ Key Implementations:
- **Tracing Context Propagation**: Enabled TraceId and SpanId propagation across REST (API Gateway) and Kafka (Event-driven).
- **Backend**: Zipkin collector and UI integrated for visualization.
- **Library**: `io.micrometer:micrometer-tracing-bridge-otel` for high-performance tracing.

## 3. Centralized Logging (ELK Stack)

We implemented structured JSON logging and centralized log aggregation.

### 📝 Key Implementations:
- **Structured Logging**: Added `logstash-logback-encoder` to the parent POM and configured `logback-spring.xml` for all services.
- **Filebeat**: A lightweight shipper configured to read logs from Docker container outputs and send them to Elasticsearch.
- **Elasticsearch**: Distributed search engine for log storage and indexing.
- **Kibana**: Powerful visualization layer for searching and analyzing logs.

## 4. Configuration Highlights

### Logback JSON Configuration (`logback-spring.xml`)
```xml
<appender name="jsonConsole" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <customFields>{"app_name":"${spring.application.name}"}</customFields>
    </encoder>
</appender>
```

### Filebeat Connector (`filebeat.yml`)
```yaml
filebeat.inputs:
- type: container
  paths:
    - '/var/lib/docker/containers/*/*.log'
output.elasticsearch:
  hosts: ["elasticsearch:9200"]
```

## 5. Verification Steps

### 1. Verify Metrics
- Access Prometheus at `http://localhost:9090`.
- Search for `pizzaflow_orders_created_total`.
- Access Grafana at `http://localhost:3000` (Default: admin/admin).

### 2. Verify Tracing
- Place an order through the API Gateway.
- Access Zipkin at `http://localhost:9411`.
- Search for traces; you should see the span starting at `api-gateway` and continuing through `order-service` and `payment-service`.

### 3. Verify Logs
- Access Kibana at `http://localhost:5601`.
- Go to "Stack Management" -> "Index Patterns" and create `filebeat-*`.
- Go to "Discover" to see real-time JSON logs with `app_name`, `traceId`, and `spanId` fields.
