-- Inventory Service Schema
CREATE TABLE IF NOT EXISTS inventory_item (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(100) NOT NULL UNIQUE,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    unit VARCHAR(50) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS stock_reservation (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    inventory_item_id BIGINT NOT NULL REFERENCES inventory_item(id),
    quantity INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(order_id, inventory_item_id)
);

CREATE TABLE IF NOT EXISTS outbox_event (
    id BIGSERIAL PRIMARY KEY,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

CREATE INDEX idx_outbox_status ON outbox_event(status);
CREATE INDEX idx_stock_reservation_order ON stock_reservation(order_id);
