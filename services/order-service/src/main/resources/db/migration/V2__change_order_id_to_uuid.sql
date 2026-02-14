
-- Enable uuid extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Add new UUID columns
ALTER TABLE orders ADD COLUMN id_new UUID DEFAULT uuid_generate_v4();
ALTER TABLE order_items ADD COLUMN id_new UUID DEFAULT uuid_generate_v4();
ALTER TABLE order_items ADD COLUMN order_id_new UUID;

-- Populate order_id_new in order_items based on existing relationships
UPDATE order_items oi
SET order_id_new = o.id_new
FROM orders o
WHERE oi.order_id = o.id;

-- Drop old foreign key constraint
ALTER TABLE order_items DROP CONSTRAINT order_items_order_id_fkey;

-- Drop old primary keys
ALTER TABLE orders DROP CONSTRAINT orders_pkey;
ALTER TABLE order_items DROP CONSTRAINT order_items_pkey;

-- Drop old ID columns
ALTER TABLE orders DROP COLUMN id;
ALTER TABLE order_items DROP COLUMN id;
ALTER TABLE order_items DROP COLUMN order_id;

-- Rename new columns to original names
ALTER TABLE orders RENAME COLUMN id_new TO id;
ALTER TABLE order_items RENAME COLUMN id_new TO id;
ALTER TABLE order_items RENAME COLUMN order_id_new TO order_id;

-- Add primary key constraints
ALTER TABLE orders ADD PRIMARY KEY (id);
ALTER TABLE order_items ADD PRIMARY KEY (id);

-- Add foreign key constraint
ALTER TABLE order_items ADD CONSTRAINT order_items_order_id_fkey
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE;

-- Make UUID columns NOT NULL
ALTER TABLE orders ALTER COLUMN id SET NOT NULL;
ALTER TABLE order_items ALTER COLUMN id SET NOT NULL;
ALTER TABLE order_items ALTER COLUMN order_id SET NOT NULL;

