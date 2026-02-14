
-- Add delivery address columns
ALTER TABLE orders ADD COLUMN delivery_street VARCHAR(255);
ALTER TABLE orders ADD COLUMN delivery_city VARCHAR(100);
ALTER TABLE orders ADD COLUMN delivery_state VARCHAR(100);
ALTER TABLE orders ADD COLUMN delivery_zip VARCHAR(20);
ALTER TABLE orders ADD COLUMN delivery_country VARCHAR(100);

-- Add longitude and latitude columns for geolocation
ALTER TABLE orders ADD COLUMN longitude DOUBLE PRECISION;
ALTER TABLE orders ADD COLUMN latitude DOUBLE PRECISION;

