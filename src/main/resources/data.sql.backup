-- Seed Data for Prompt Templates
-- This file is automatically executed by Spring Boot on startup

-- 1. Geriatric Medication Recommendation
INSERT INTO prompt_templates (id, name, description, category, template, medical_context, safety_guidelines, expected_variables, version, is_active, created_at, updated_at)
VALUES (
    RANDOM_UUID(),
    'Geriatric Medication Recommendation',
    'Generate appropriate medication recommendations for elderly patients',
    'MEDICATION_RECOMMENDATION',
    'Recommend medications for ${patientName}, age ${age}, with diagnosis: ${diagnosis}.

Patient Profile:
- Current medications: ${currentMedications}
- Allergies: ${allergies}
- Renal function: ${renalFunction}
- Hepatic function: ${hepaticFunction}
- Other conditions: ${otherConditions}

Consider:
1. Age-appropriate dosing (Beers Criteria)
2. Drug-disease interactions
3. Polypharmacy risks
4. Cost and adherence factors

Provide:
- Recommended medication(s) with rationale
- Starting dose appropriate for elderly
- Monitoring requirements
- Patient education points
- Alternative options if contraindicated',
    'For evidence-based medication selection in geriatric patients',
    'CRITICAL: Always check renal/hepatic function before dosing. Avoid high-risk medications in elderly per Beers Criteria.',
    'patientName, age, diagnosis, currentMedications, allergies, renalFunction, hepaticFunction, otherConditions',
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 2. Drug Interaction Checker
INSERT INTO prompt_templates (id, name, description, category, template, medical_context, safety_guidelines, expected_variables, version, is_active, created_at, updated_at)
VALUES (
    RANDOM_UUID(),
    'Drug Interaction Checker',
    'Analyze potential drug-drug interactions in medication regimen',
    'DRUG_INTERACTION',
    'Analyze drug interactions for ${patientName}, age ${age}.

Current Medication List:
${medicationList}

Additional Information:
- OTC medications: ${otcMedications}
- Supplements: ${supplements}
- Recent additions: ${recentChanges}

Analyze:
1. Drug-Drug Interactions
   - Major interactions (contraindicated)
   - Moderate interactions (caution needed)
   - Minor interactions (monitoring)

2. Food-Drug Interactions
3. Timing conflicts
4. Duplicate therapy

For each interaction found, provide:
- Severity level (Critical/Major/Moderate/Minor)
- Clinical significance
- Mechanism of interaction
- Management recommendations
- Alternative medications if needed',
    'For comprehensive drug interaction screening in polypharmacy',
    'CRITICAL: Flag all major/critical interactions immediately. Consider pharmacokinetic changes in elderly.',
    'patientName, age, medicationList, otcMedications, supplements, recentChanges',
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 3. Geriatric Dosage Calculator
INSERT INTO prompt_templates (id, name, description, category, template, medical_context, safety_guidelines, expected_variables, version, is_active, created_at, updated_at)
VALUES (
    RANDOM_UUID(),
    'Geriatric Dosage Calculator',
    'Calculate age-appropriate medication dosages',
    'DOSAGE_CALCULATION',
    'Calculate appropriate dose for ${patientName}, age ${age}, weight ${weight}kg.

Medication: ${medicationName}
Standard Adult Dose: ${standardDose}

Patient Factors:
- Weight: ${weight}kg
- Height: ${height}cm
- BMI: ${bmi}
- Renal function (CrCl): ${creatinineClearance} mL/min
- Hepatic function: ${hepaticStatus}
- Albumin: ${albumin} g/dL

Calculate:
1. Initial starting dose for elderly patient
2. Renal dose adjustment if needed (using Cockcroft-Gault)
3. Hepatic dose adjustment if needed
4. Maximum safe dose
5. Titration schedule

Provide:
- Recommended starting dose with rationale
- Dose adjustments needed
- Frequency recommendations
- Maximum dose limits
- Red flags/contraindications
- Monitoring parameters',
    'For safe and appropriate dosing in geriatric patients',
    'CRITICAL: Start low, go slow in elderly. Always adjust for renal function. Double-check calculations.',
    'patientName, age, weight, medicationName, standardDose, height, bmi, creatinineClearance, hepaticStatus, albumin',
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 4. Geriatric Symptom Analyzer
INSERT INTO prompt_templates (id, name, description, category, template, medical_context, safety_guidelines, expected_variables, version, is_active, created_at, updated_at)
VALUES (
    RANDOM_UUID(),
    'Geriatric Symptom Analyzer',
    'Analyze patient symptoms and suggest appropriate actions',
    'SYMPTOM_ANALYSIS',
    'Analyze symptoms for ${patientName}, age ${age}.

Presenting Symptoms:
${symptoms}

Duration: ${duration}
Severity (1-10): ${severity}
Onset: ${onset}

Patient Background:
- Current diagnoses: ${diagnoses}
- Current medications: ${medications}
- Recent medication changes: ${recentMedChanges}
- Vital signs: ${vitalSigns}

Analyze:
1. Possible causes of symptoms
   - Disease progression
   - New condition
   - Medication side effect
   - Drug interaction
   - Drug withdrawal

2. Red flags requiring immediate attention
3. Atypical presentations common in elderly
4. Risk assessment (low/moderate/high urgency)

Provide:
- Most likely causes (differential)
- Recommended immediate actions
- When to seek emergency care
- Suggested monitoring
- Medication review needed
- Follow-up timeframe',
    'For systematic symptom evaluation in elderly patients',
    'CRITICAL: Elderly often have atypical presentations. Consider medication side effects first. Low threshold for emergency evaluation.',
    'patientName, age, symptoms, duration, severity, onset, diagnoses, medications, recentMedChanges, vitalSigns',
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 5. Adverse Drug Event Detector
INSERT INTO prompt_templates (id, name, description, category, template, medical_context, safety_guidelines, expected_variables, version, is_active, created_at, updated_at)
VALUES (
    RANDOM_UUID(),
    'Adverse Drug Event Detector',
    'Detect and analyze potential adverse drug events',
    'ADVERSE_EVENT',
    'Evaluate potential adverse drug event for ${patientName}, age ${age}.

Event Description:
${eventDescription}

Timing:
- Event onset: ${eventOnset}
- Medication started: ${medicationStartDate}
- Time relationship: ${timeRelationship}

Suspected Medication:
${suspectedMedication}
Dose: ${dose}
Duration of use: ${duration}

Patient Factors:
- Age: ${age}
- Comorbidities: ${comorbidities}
- Other medications: ${otherMedications}
- Renal/hepatic function: ${organFunction}

Naranjo Assessment:
Evaluate likelihood this is an adverse drug reaction:
1. Temporal relationship
2. Known reaction to this drug
3. Event improved with discontinuation?
4. Rechallenge attempted?
5. Alternative causes ruled out?
6. Dose-response relationship
7. Previous patient reaction history

Provide:
- Probability score (Definite/Probable/Possible/Doubtful)
- Clinical assessment
- Recommended actions:
  * Continue medication
  * Dose reduction
  * Discontinue and substitute
  * Monitor closely
- Reporting requirements
- Patient counseling points
- Documentation needed',
    'For systematic evaluation of adverse drug events in elderly',
    'CRITICAL: When in doubt, assume drug-related until proven otherwise. Report serious ADEs per protocol.',
    'patientName, age, eventDescription, eventOnset, medicationStartDate, timeRelationship, suspectedMedication, dose, duration, comorbidities, otherMedications, organFunction',
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 6. Comprehensive Medication Review
INSERT INTO prompt_templates (id, name, description, category, template, medical_context, safety_guidelines, expected_variables, version, is_active, created_at, updated_at)
VALUES (
    RANDOM_UUID(),
    'Comprehensive Medication Review',
    'Complete medication regimen review and optimization',
    'MEDICATION_REVIEW',
    'Comprehensive medication review for ${patientName}, age ${age}.

Complete Medication List:
${completeRegimen}

Patient Information:
- Diagnoses: ${diagnoses}
- Allergies: ${allergies}
- Recent labs: ${labs}
- Functional status: ${functionalStatus}
- Cognitive status: ${cognitiveStatus}
- Social support: ${socialSupport}

Review Components:

1. INDICATION REVIEW
   - Each medication has valid indication?
   - Any untreated conditions?

2. EFFECTIVENESS
   - Medications achieving goals?
   - Therapeutic drug monitoring needed?

3. SAFETY
   - Drug interactions (drug-drug, drug-disease)
   - Beers Criteria violations
   - Age-inappropriate doses
   - Monitoring requirements met?

4. ADHERENCE
   - Pill burden assessment
   - Cost barriers
   - Administration complexity
   - Side effect tolerability

5. DEPRESCRIBING OPPORTUNITIES
   - Medications no longer needed
   - Duplicate therapy
   - Safer alternatives available

Provide:
- Summary of issues found (categorized by severity)
- Specific recommendations with rationale
- Prioritized action plan
- Deprescribing suggestions with tapering if needed
- Monitoring plan
- Patient/caregiver education topics',
    'For complete medication therapy management in elderly',
    'CRITICAL: Consider whole patient, not just medications. Involve patient in decisions. Document rationale for all changes.',
    'patientName, age, completeRegimen, diagnoses, allergies, labs, functionalStatus, cognitiveStatus, socialSupport',
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 7. Personalized Care Plan Generator
INSERT INTO prompt_templates (id, name, description, category, template, medical_context, safety_guidelines, expected_variables, version, is_active, created_at, updated_at)
VALUES (
    RANDOM_UUID(),
    'Personalized Care Plan Generator',
    'Template for generating individualized care plans',
    'CARE_PLAN',
    'Create a personalized care plan for ${patientName}, ${age} years old, diagnosed with ${diagnoses}.

Patient Profile:
- Living situation: ${livingSituation}
- Support system: ${supportSystem}
- Functional status: ${functionalStatus}
- Goals: ${patientGoals}

Care Plan Components:
1. Medical Management:
   - Medications: ${medications}
   - Monitoring: ${monitoring}

2. Daily Care:
   - Personal care: ${personalCare}
   - Nutrition: ${nutrition}
   - Exercise: ${exercise}

3. Safety:
   - Fall risk: ${fallRisk}
   - Home modifications: ${homeModifications}

Generate comprehensive, actionable care plan with timeline and metrics.',
    'For creating individualized care plans for elderly patients',
    'Ensure care plan is realistic and considers patient preferences.',
    'patientName, age, diagnoses, livingSituation, supportSystem, functionalStatus, patientGoals, medications, monitoring, personalCare, nutrition, exercise, fallRisk, homeModifications',
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
