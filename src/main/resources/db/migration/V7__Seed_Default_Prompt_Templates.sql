-- Seed Default Prompt Templates
-- File: src/main/resources/db/migration/V7__Seed_Default_Prompt_Templates.sql

-- 1. Medication Recommendation Template
INSERT INTO prompt_templates (
    id, name, category, template, version, is_active,
    medical_context, safety_guidelines, expected_variables, description, created_at
) VALUES (
    RANDOM_UUID(),
    'medication-recommendation',
    'MEDICATION_RECOMMENDATION',
    'Patient Profile:
- Age: ${patientAge} years
- Weight: ${patientWeight} kg
- Gender: ${patientGender}
- Medical Conditions: ${medicalConditions}
- Current Medications: ${currentMedications}
- Known Allergies: ${allergies}

Clinical Situation:
${clinicalSituation}

Based on this geriatric patient profile, please recommend appropriate medications. For each recommendation, provide:
1. Medication name (generic and brand if applicable)
2. Recommended dosage and frequency
3. Route of administration
4. Clinical rationale for this choice
5. Potential side effects to monitor (especially geriatric-specific)
6. Drug interactions with current medications
7. Alternative options if applicable
8. Special considerations for elderly patients

Please format your response as a JSON array with the following structure:
{
  "recommendations": [
    {
      "medicationName": "...",
      "genericName": "...",
      "dosage": "...",
      "frequency": "...",
      "route": "...",
      "rationale": "...",
      "sideEffects": ["..."],
      "interactions": ["..."],
      "alternatives": ["..."],
      "geriatricConsiderations": "...",
      "confidenceScore": 0.0-1.0
    }
  ],
  "warnings": ["..."],
  "additionalNotes": "..."
}',
    1,
    TRUE,
    'You are a clinical pharmacist specializing in geriatric medicine. Your recommendations must follow evidence-based guidelines and consider age-related pharmacokinetic and pharmacodynamic changes. Always apply the Beers Criteria for potentially inappropriate medications in older adults. Prioritize patient safety above all else.',
    '1. Always check for drug-drug interactions
2. Consider renal and hepatic function in elderly patients
3. Start with lowest effective dose (start low, go slow)
4. Avoid medications on the Beers Criteria when possible
5. Consider fall risk and anticholinergic burden
6. Monitor for adverse drug events carefully
7. Ensure recommendations are evidence-based
8. Flag any contraindications clearly',
    'patientAge,patientWeight,patientGender,medicalConditions,currentMedications,allergies,clinicalSituation',
    'Template for generating medication recommendations for geriatric patients',
    CURRENT_TIMESTAMP
);

-- 2. Drug Interaction Analysis Template
INSERT INTO prompt_templates (
    id, name, category, template, version, is_active,
    medical_context, safety_guidelines, expected_variables, description, created_at
) VALUES (
    RANDOM_UUID(),
    'drug-interaction-analysis',
    'DRUG_INTERACTION',
    'Analyze potential drug interactions for the following medication regimen:

Medications:
${medications}

Patient Information:
- Age: ${patientAge} years
- Renal Function: ${renalFunction}
- Hepatic Function: ${hepaticFunction}
- Current Medical Conditions: ${medicalConditions}

Please provide a comprehensive drug interaction analysis including:

1. **Major Interactions** (Contraindicated or severe)
2. **Moderate Interactions** (Require monitoring or dose adjustment)
3. **Minor Interactions** (Low clinical significance)

For each interaction, provide:
- Interacting medications
- Mechanism of interaction
- Clinical significance
- Recommended action
- Monitoring parameters
- Alternative medications if needed

Format as JSON:
{
  "majorInteractions": [...],
  "moderateInteractions": [...],
  "minorInteractions": [...],
  "overallRiskAssessment": "...",
  "recommendations": [...]
}',
    1,
    TRUE,
    'You are a clinical pharmacist expert in drug interactions and pharmacotherapy. Analyze interactions based on mechanism, severity, and clinical significance. Consider patient-specific factors including age, organ function, and comorbidities.',
    '1. Identify all potential drug-drug interactions
2. Consider pharmacokinetic and pharmacodynamic interactions
3. Assess clinical significance in context of patient factors
4. Provide actionable recommendations
5. Suggest safer alternatives when appropriate
6. Consider food-drug and disease-drug interactions',
    'medications,patientAge,renalFunction,hepaticFunction,medicalConditions',
    'Template for comprehensive drug interaction analysis',
    CURRENT_TIMESTAMP
);

-- 3. Dosage Calculation Template
INSERT INTO prompt_templates (
    id, name, category, template, version, is_active,
    medical_context, safety_guidelines, expected_variables, description, created_at
) VALUES (
    RANDOM_UUID(),
    'dosage-calculation',
    'DOSAGE_CALCULATION',
    'Calculate appropriate dosage for:

Medication: ${medicationName}
Patient Age: ${patientAge} years
Patient Weight: ${patientWeight} kg
Indication: ${indication}
Renal Function (CrCl): ${creatinineClearance} mL/min
Hepatic Function: ${hepaticFunction}

Additional Factors:
${additionalFactors}

Please calculate:
1. Initial dose
2. Maintenance dose
3. Frequency of administration
4. Route of administration
5. Dose adjustments needed for renal/hepatic impairment
6. Maximum safe dose
7. Monitoring parameters

Provide detailed rationale for each calculation, citing relevant guidelines or studies.

Format as JSON:
{
  "initialDose": "...",
  "maintenanceDose": "...",
  "frequency": "...",
  "route": "...",
  "adjustments": {...},
  "maximumDose": "...",
  "rationale": "...",
  "monitoring": [...],
  "warnings": [...]
}',
    1,
    TRUE,
    'You are a clinical pharmacist with expertise in pharmacokinetics and dose optimization. Calculate doses based on patient-specific parameters, considering age-related changes in drug metabolism and organ function. Always prioritize safety and provide conservative estimates for elderly patients.',
    '1. Use evidence-based dosing guidelines
2. Adjust for renal/hepatic impairment
3. Consider drug interactions affecting dosing
4. Apply weight-based dosing appropriately
5. Account for geriatric pharmacokinetic changes
6. Provide monitoring parameters
7. Include maximum safe doses
8. Flag when therapeutic drug monitoring is needed',
    'medicationName,patientAge,patientWeight,indication,creatinineClearance,hepaticFunction,additionalFactors',
    'Template for personalized dosage calculations',
    CURRENT_TIMESTAMP
);

-- 4. Care Plan Generation Template
INSERT INTO prompt_templates (
    id, name, category, template, version, is_active,
    medical_context, safety_guidelines, expected_variables, description, created_at
) VALUES (
    RANDOM_UUID(),
    'care-plan-generation',
    'CARE_PLAN',
    'Generate a comprehensive care plan for:

Patient Profile:
- Age: ${patientAge} years
- Medical Conditions: ${medicalConditions}
- Functional Status: ${functionalStatus}
- Cognitive Status: ${cognitiveStatus}
- Living Situation: ${livingSituation}
- Support System: ${supportSystem}
- Current Medications: ${currentMedications}

Care Goals:
${careGoals}

Please create a personalized care plan including:

1. **Medical Management**
   - Medication management strategies
   - Monitoring requirements
   - Follow-up schedule

2. **Activities of Daily Living (ADL) Support**
   - Personal care needs
   - Mobility assistance
   - Safety measures

3. **Nutrition and Hydration**
   - Dietary recommendations
   - Nutritional monitoring
   - Special considerations

4. **Cognitive and Emotional Support**
   - Mental health strategies
   - Social engagement activities
   - Family involvement

5. **Safety and Fall Prevention**
   - Environmental modifications
   - Risk assessments
   - Emergency protocols

6. **Care Team Coordination**
   - Healthcare provider roles
   - Caregiver responsibilities
   - Communication protocols

Format as JSON with specific, actionable tasks.',
    1,
    TRUE,
    'You are a geriatric care specialist creating evidence-based, person-centered care plans. Consider the whole person including physical, cognitive, emotional, and social needs. Ensure recommendations are practical and achievable.',
    '1. Create realistic, achievable goals
2. Consider patient and family preferences
3. Address safety as top priority
4. Include measurable outcomes
5. Coordinate care across providers
6. Plan for potential complications
7. Include caregiver support
8. Ensure cultural sensitivity',
    'patientAge,medicalConditions,functionalStatus,cognitiveStatus,livingSituation,supportSystem,currentMedications,careGoals',
    'Template for generating comprehensive geriatric care plans',
    CURRENT_TIMESTAMP
);

-- Create indexes for faster template retrieval
CREATE INDEX IF NOT EXISTS idx_prompt_templates_name_active ON prompt_templates(name, is_active);
CREATE INDEX IF NOT EXISTS idx_prompt_templates_category_active ON prompt_templates(category, is_active);