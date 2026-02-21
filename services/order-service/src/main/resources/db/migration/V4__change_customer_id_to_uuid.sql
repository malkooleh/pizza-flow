-- Enable uuid extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Change customer_id column to UUID type
ALTER TABLE orders ALTER COLUMN customer_id TYPE UUID USING uuid_generate_v4();

-- Add NOT NULL constraint
ALTER TABLE orders ALTER COLUMN customer_id SET NOT NULL;

