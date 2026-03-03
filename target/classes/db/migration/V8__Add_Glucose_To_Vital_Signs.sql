-- Add glucose field to vital_signs table
ALTER TABLE vital_signs ADD COLUMN IF NOT EXISTS glucose DECIMAL(6,2);
COMMENT ON COLUMN vital_signs.glucose IS 'Blood glucose level in mg/dL';
