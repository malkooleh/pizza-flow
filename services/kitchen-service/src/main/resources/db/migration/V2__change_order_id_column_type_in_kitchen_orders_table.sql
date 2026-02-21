-- Enable uuid extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Drop any existing constraints or indexes on order_id if needed
ALTER TABLE kitchen_orders DROP CONSTRAINT IF EXISTS kitchen_orders_id_key;

-- Change order_id column type to UUID and generate UUIDs for existing rows
ALTER TABLE kitchen_orders ALTER COLUMN order_id TYPE UUID USING uuid_generate_v4();

-- Add NOT NULL constraint
ALTER TABLE kitchen_orders ALTER COLUMN order_id SET NOT NULL;

-- Add the unique constraint back
ALTER TABLE kitchen_orders ADD CONSTRAINT uk_kitchen_orders_id UNIQUE (order_id);
