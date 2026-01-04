-- Add retry_count column to outbox_event table
ALTER TABLE outbox_event
ADD COLUMN IF NOT EXISTS retry_count INTEGER NOT NULL DEFAULT 0;
