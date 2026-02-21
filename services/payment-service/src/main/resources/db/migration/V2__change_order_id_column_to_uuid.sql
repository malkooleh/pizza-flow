-- Enable uuid extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Change order_id column to UUID type (using a valid UUID conversion)
ALTER TABLE payments ALTER COLUMN order_id TYPE UUID USING uuid_generate_v4();

-- Add NOT NULL constraint
ALTER TABLE payments ALTER COLUMN order_id SET NOT NULL;