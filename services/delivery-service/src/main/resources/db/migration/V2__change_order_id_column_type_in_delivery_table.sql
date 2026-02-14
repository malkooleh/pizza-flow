-- Drop any existing constraints or indexes on order_id if needed
ALTER TABLE delivery DROP CONSTRAINT IF EXISTS delivery_order_id_key;

-- Change order_id column type to UUID and generate UUIDs for existing rows
ALTER TABLE delivery ALTER COLUMN order_id TYPE UUID USING gen_random_uuid();

-- Add NOT NULL constraint
ALTER TABLE delivery ALTER COLUMN order_id SET NOT NULL;

-- Add the unique constraint back
ALTER TABLE delivery ADD CONSTRAINT uk_delivery_order_id UNIQUE (order_id);
