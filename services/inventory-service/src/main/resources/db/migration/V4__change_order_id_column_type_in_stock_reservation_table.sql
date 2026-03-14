-- Enable uuid extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Drop any existing constraints or indexes on order_id if needed
ALTER TABLE stock_reservation DROP CONSTRAINT IF EXISTS stock_reservation_order_id_inventory_item_id_key;
DROP INDEX IF EXISTS idx_stock_reservation_order;

-- Change order_id column type to UUID and generate UUIDs for existing rows
ALTER TABLE stock_reservation ALTER COLUMN order_id TYPE UUID USING uuid_generate_v4();

-- Add NOT NULL constraint
ALTER TABLE stock_reservation ALTER COLUMN order_id SET NOT NULL;

-- Add the unique constraint/index back
CREATE UNIQUE INDEX stock_reservation_order_id_inventory_item_id_key ON stock_reservation(order_id, inventory_item_id);
CREATE INDEX idx_stock_reservation_order ON stock_reservation(order_id);