-- Initialize Geriatric Care Database
-- This script runs automatically when the database container first starts

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set timezone
SET timezone = 'UTC';

-- Create a read-only user for reporting (optional)
-- CREATE USER geriatric_readonly WITH PASSWORD 'readonly_pass';
-- GRANT CONNECT ON DATABASE geriatric_care TO geriatric_readonly;
-- GRANT SELECT ON ALL TABLES IN SCHEMA public TO geriatric_readonly;

-- Log initialization
SELECT 'Database initialized successfully at ' || NOW() AS status;
