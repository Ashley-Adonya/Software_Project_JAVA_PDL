-- Script to set campaign status to PREPARATION
-- This allows testing the workflow from the beginning

UPDATE campaigns 
SET status = 'PREPARATION',
    opened_at = NULL,
    closed_at = NULL,
    processed_at = NULL,
    validated_at = NULL,
    archived_at = NULL
WHERE name = 'Campagne ING3 2026';

COMMIT;

-- Verify the change
SELECT id, name, status, opened_at, closed_at, processed_at, validated_at, archived_at 
FROM campaigns 
WHERE name = 'Campagne ING3 2026';