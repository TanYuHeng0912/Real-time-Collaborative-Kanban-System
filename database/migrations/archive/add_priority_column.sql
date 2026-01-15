-- Migration: Add priority column to cards table
-- This migration adds the priority column to support card prioritization (LOW, MEDIUM, HIGH, DONE)

-- Add priority column with default value
ALTER TABLE cards 
ADD COLUMN IF NOT EXISTS priority VARCHAR(20) DEFAULT 'MEDIUM';

-- Update existing cards to have MEDIUM priority if they don't have one
UPDATE cards 
SET priority = 'MEDIUM' 
WHERE priority IS NULL;

-- Add comment to column
COMMENT ON COLUMN cards.priority IS 'Card priority: LOW, MEDIUM, HIGH, or DONE';

