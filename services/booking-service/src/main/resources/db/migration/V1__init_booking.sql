CREATE TABLE restaurant_tables (
    id BIGSERIAL PRIMARY KEY,
    table_number INT NOT NULL UNIQUE,
    capacity INT NOT NULL
);

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL,
    table_id BIGINT REFERENCES restaurant_tables(id),
    booking_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO restaurant_tables (table_number, capacity) VALUES (1, 2);
INSERT INTO restaurant_tables (table_number, capacity) VALUES (2, 2);
INSERT INTO restaurant_tables (table_number, capacity) VALUES (3, 4);
INSERT INTO restaurant_tables (table_number, capacity) VALUES (4, 4);
INSERT INTO restaurant_tables (table_number, capacity) VALUES (5, 6);
