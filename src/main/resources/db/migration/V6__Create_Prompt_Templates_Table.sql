-- Database Migration: Create Prompt Templates Table
-- File: src/main/resources/db/migration/V6__Create_Prompt_Templates_Table.sql

CREATE TABLE prompt_templates (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    template TEXT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    medical_context TEXT,
    safety_guidelines TEXT,
    expected_variables TEXT,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    
    CONSTRAINT uq_prompt_name_version UNIQUE (name, version),
    CONSTRAINT fk_prompt_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_prompt_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes for better query performance
CREATE INDEX idx_prompt_category ON prompt_templates(category);
CREATE INDEX idx_prompt_active ON prompt_templates(is_active);
CREATE INDEX idx_prompt_name ON prompt_templates(name);
CREATE INDEX idx_prompt_version ON prompt_templates(version DESC);

-- Add comments for documentation
COMMENT ON TABLE prompt_templates IS 'Versioned prompt templates for AI interactions';
COMMENT ON COLUMN prompt_templates.template IS 'Prompt template with ${variable} placeholders';
COMMENT ON COLUMN prompt_templates.version IS 'Template version number (incrementing)';
COMMENT ON COLUMN prompt_templates.medical_context IS 'Medical context and guidelines for the AI';
COMMENT ON COLUMN prompt_templates.safety_guidelines IS 'Safety considerations and warnings';
COMMENT ON COLUMN prompt_templates.expected_variables IS 'Comma-separated list of required variables';