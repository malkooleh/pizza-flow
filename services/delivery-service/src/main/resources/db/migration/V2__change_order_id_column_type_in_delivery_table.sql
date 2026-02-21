-- Enable uuid extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Change order_id column type to UUID and generate UUIDs for existing rows
ALTER TABLE delivery ALTER COLUMN order_id TYPE UUID USING uuid_generate_v4();

-- Add NOT NULL constraint
ALTER TABLE delivery ALTER COLUMN order_id SET NOT NULL;
