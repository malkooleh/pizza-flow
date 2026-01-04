-- Add last_attempt_at column for exponential backoff tracking
ALTER TABLE outbox_event
ADD COLUMN IF NOT EXISTS last_attempt_at TIMESTAMP;
