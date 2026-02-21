-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS courier (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    current_location GEOMETRY(Point, 4326),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS delivery (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    courier_id BIGINT REFERENCES courier(id),
    status VARCHAR(50) NOT NULL,
    delivery_location GEOMETRY(Point, 4326) NOT NULL,
    street VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    country VARCHAR(100),
    assigned_at TIMESTAMP,
    picked_up_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS delivery_zone (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    area GEOMETRY(Polygon, 4326) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_courier_location ON courier USING GIST(current_location);
CREATE INDEX IF NOT EXISTS idx_delivery_location ON delivery USING GIST(delivery_location);
CREATE INDEX IF NOT EXISTS idx_delivery_zone_area ON delivery_zone USING GIST(area);
CREATE INDEX IF NOT EXISTS idx_delivery_status ON delivery(status);
CREATE INDEX IF NOT EXISTS idx_courier_status ON courier(status);
