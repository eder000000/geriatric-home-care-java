-- H2 Database Migration: Create AI Audit Logs Table
-- File: src/main/resources/db/migration/V5__Create_AI_Audit_Logs_Table.sql

CREATE TABLE ai_audit_logs (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    request_type VARCHAR(50) NOT NULL,
    prompt CLOB,
    response CLOB,
    user_id UUID,
    patient_id UUID,
    confidence_score DOUBLE,
    tokens_used INT,
    response_time_ms BIGINT,
    approved BOOLEAN DEFAULT FALSE,
    approved_by UUID,
    approved_at TIMESTAMP,
    error_message CLOB,
    success BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create indexes for better query performance
CREATE INDEX idx_ai_audit_timestamp ON ai_audit_logs(timestamp DESC);
CREATE INDEX idx_ai_audit_user_id ON ai_audit_logs(user_id);
CREATE INDEX idx_ai_audit_patient_id ON ai_audit_logs(patient_id);
CREATE INDEX idx_ai_audit_approved ON ai_audit_logs(approved);
CREATE INDEX idx_ai_audit_success ON ai_audit_logs(success);
CREATE INDEX idx_ai_audit_request_type ON ai_audit_logs(request_type);