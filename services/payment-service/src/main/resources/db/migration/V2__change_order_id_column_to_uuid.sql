-- Enable uuid extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Drop any existing constraints or indexes on order_id if needed
ALTER TABLE payments DROP CONSTRAINT IF EXISTS idx_payment_order_id;

-- Change order_id column to UUID type
ALTER TABLE payments ALTER COLUMN order_id TYPE UUID USING order_id::uuid;

-- Add NOT NULL constraint
ALTER TABLE payments ALTER COLUMN order_id SET NOT NULL;

-- Add the unique constraint back
ALTER TABLE payments ADD CONSTRAINT idx_payment_order_id UNIQUE (order_id);