-- V7: Seed Default Prompt Templates

-- Health Assessment Template
INSERT INTO prompt_templates (id, name, description, category, template, medical_context, safety_guidelines, expected_variables, version, is_active, created_at, updated_at)
VALUES (
    RANDOM_UUID(),
    'Comprehensive Health Assessment',
    'Template for conducting comprehensive geriatric health assessments',
    'HEALTH_ASSESSMENT',
    'Conduct a comprehensive health assessment for ${patientName}, age ${age}, with the following conditions: ${conditions}.

Assessment Areas:
1. Physical Health:
   - Vital signs: ${vitalSigns}
   - Mobility: ${mobilityStatus}
   - Pain level: ${painLevel}

2. Cognitive Function:
   - Mental status: ${mentalStatus}
   - Memory concerns: ${memoryConcerns}

3. Daily Living Activities:
   - ADL status: ${adlStatus}
   - IADL status: ${iadlStatus}

4. Medications:
   - Current medications: ${medications}
   - Adherence: ${adherence}

Based on this assessment, provide:
- Overall health status summary
- Risk factors identified
- Recommended interventions
- Follow-up recommendations',
    'For geriatric patients requiring comprehensive health evaluation',
    'Ensure all vital signs are measured accurately. Respect patient dignity during assessment. Note any signs of distress or discomfort.',
    'patientName, age, conditions, vitalSigns, mobilityStatus, painLevel, mentalStatus, memoryConcerns, adlStatus, iadlStatus, medications, adherence',
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Care Plan Generation Template
INSERT INTO prompt_templates (id, name, description, category, template, medical_context, safety_guidelines, expected_variables, version, is_active, created_at, updated_at)
VALUES (
    RANDOM_UUID(),
    'Personalized Care Plan Generator',
    'Template for generating individualized care plans based on patient needs',
    'CARE_PLAN',
    'Create a personalized care plan for ${patientName}, ${age} years old, diagnosed with ${diagnoses}.

Patient Profile:
- Living situation: ${livingSituation}
- Support system: ${supportSystem}
- Current functional status: ${functionalStatus}
- Goals: ${patientGoals}

Care Plan Components:
1. Medical Management:
   - Medications: ${medications}
   - Required monitoring: ${monitoring}
   - Specialist care needed: ${specialists}

2. Daily Care Activities:
   - Personal care needs: ${personalCare}
   - Nutrition requirements: ${nutrition}
   - Exercise/mobility plan: ${exercise}

3. Safety Considerations:
   - Fall risk: ${fallRisk}
   - Home modifications needed: ${homeModifications}
   - Emergency protocols: ${emergencyProtocols}

4. Psychosocial Support:
   - Mental health needs: ${mentalHealth}
   - Social engagement: ${socialEngagement}
   - Family involvement: ${familyInvolvement}

Generate a comprehensive, actionable care plan with specific interventions, timeline, and success metrics.',
    'For creating individualized care plans for elderly patients with multiple comorbidities',
    'Ensure care plan is realistic and considers patient preferences. Include family/caregiver input. Address safety concerns explicitly.',
    'patientName, age, diagnoses, livingSituation, supportSystem, functionalStatus, patientGoals, medications, monitoring, specialists, personalCare, nutrition, exercise, fallRisk, homeModifications, emergencyProtocols, mentalHealth, socialEngagement, familyInvolvement',
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Medication Review Template
INSERT INTO prompt_templates (id, name, description, category, template, medical_context, safety_guidelines, expected_variables, version, is_active, created_at, updated_at)
VALUES (
    RANDOM_UUID(),
    'Medication Safety Review',
    'Template for comprehensive medication review and optimization',
    'MEDICATION',
    'Perform a comprehensive medication review for ${patientName}, age ${age}.

Current Medication List:
${currentMedications}

Patient Information:
- Diagnoses: ${diagnoses}
- Allergies: ${allergies}
- Renal function: ${renalFunction}
- Hepatic function: ${hepaticFunction}
- Recent labs: ${recentLabs}

Review Focus Areas:
1. Drug-Drug Interactions
   - Identify potential interactions
   - Assess severity and clinical significance

2. Drug-Disease Interactions
   - Check for contraindications
   - Identify medications potentially worsening conditions

3. Dosing Appropriateness
   - Age-appropriate dosing
   - Renal/hepatic dose adjustments needed
   - Beers Criteria considerations

4. Medication Burden
   - Pill burden assessment
   - Simplification opportunities
   - Deprescribing candidates

5. Adherence Barriers
   - Cost considerations
   - Administration complexity
   - Side effect profile

Provide:
- Identified issues with risk levels
- Specific recommendations for optimization
- Alternative medication suggestions if appropriate
- Monitoring requirements',
    'For geriatric medication review to optimize therapy and reduce adverse events',
    'CRITICAL: Flag all serious drug interactions immediately. Consider age-related pharmacokinetic changes. Never recommend discontinuing medications without physician consultation.',
    'patientName, age, currentMedications, diagnoses, allergies, renalFunction, hepaticFunction, recentLabs',
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Fall Risk Assessment Template
INSERT INTO prompt_templates (id, name, description, category, template, medical_context, safety_guidelines, expected_variables, version, is_active, created_at, updated_at)
VALUES (
    RANDOM_UUID(),
    'Fall Risk Assessment and Prevention',
    'Template for evaluating fall risk and creating prevention strategies',
    'HEALTH_ASSESSMENT',
    'Conduct fall risk assessment for ${patientName}, age ${age}.

Fall History:
- Previous falls: ${previousFalls}
- Circumstances: ${fallCircumstances}
- Injuries sustained: ${injuries}

Risk Factors Assessment:
1. Intrinsic Factors:
   - Gait/balance: ${gaitBalance}
   - Vision: ${visionStatus}
   - Cognition: ${cognition}
   - Medications: ${medications}
   - Chronic conditions: ${conditions}
   - Orthostatic hypotension: ${orthostaticStatus}

2. Extrinsic Factors:
   - Home environment: ${homeEnvironment}
   - Footwear: ${footwear}
   - Assistive devices: ${assistiveDevices}
   - Lighting: ${lighting}

3. Functional Status:
   - Mobility: ${mobility}
   - ADL independence: ${adlStatus}
   - Use of bathroom at night: ${nighttimeMobility}

Based on this assessment, provide:
1. Overall fall risk level (low/moderate/high)
2. Primary risk factors identified
3. Specific interventions to reduce risk:
   - Medical interventions
   - Environmental modifications
   - Exercise/therapy recommendations
   - Assistive device recommendations
4. Monitoring plan
5. Education topics for patient/family',
    'For comprehensive fall risk evaluation and prevention planning in elderly patients',
    'Falls are a leading cause of injury in elderly. Take all risk factors seriously. Recommend immediate intervention for high-risk patients.',
    'patientName, age, previousFalls, fallCircumstances, injuries, gaitBalance, visionStatus, cognition, medications, conditions, orthostaticStatus, homeEnvironment, footwear, assistiveDevices, lighting, mobility, adlStatus, nighttimeMobility',
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Nutrition Assessment Template
INSERT INTO prompt_templates (id, name, description, category, template, medical_context, safety_guidelines, expected_variables, version, is_active, created_at, updated_at)
VALUES (
    RANDOM_UUID(),
    'Geriatric Nutrition Assessment',
    'Template for evaluating nutritional status and creating dietary plans',
    'HEALTH_ASSESSMENT',
    'Perform nutrition assessment for ${patientName}, age ${age}, weight ${weight}kg, height ${height}cm.

Nutritional Status:
- BMI: ${bmi}
- Weight change: ${weightChange}
- Appetite: ${appetite}
- Dietary restrictions: ${dietaryRestrictions}

Medical Considerations:
- Diagnoses: ${diagnoses}
- Medications affecting nutrition: ${medications}
- Swallowing status: ${swallowingStatus}
- Dental/oral health: ${oralHealth}

Current Dietary Intake:
- Typical daily intake: ${dailyIntake}
- Fluid intake: ${fluidIntake}
- Supplement use: ${supplements}
- Eating assistance needed: ${eatingAssistance}

Functional Status:
- Ability to shop: ${shopping}
- Ability to prepare meals: ${mealPrep}
- Financial constraints: ${financialStatus}

Assessment Focus:
1. Malnutrition risk screening
2. Micronutrient deficiencies
3. Hydration status
4. Disease-specific nutrition needs
5. Barriers to adequate nutrition

Provide:
- Nutritional risk level
- Identified deficiencies or concerns
- Specific dietary recommendations
- Supplement recommendations if needed
- Referrals needed (dietitian, speech therapy, etc.)
- Practical strategies for patient/caregiver',
    'For comprehensive nutritional assessment in geriatric patients',
    'Malnutrition is common in elderly and often overlooked. Screen for dysphagia before making dietary recommendations. Consider cultural food preferences.',
    'patientName, age, weight, height, bmi, weightChange, appetite, dietaryRestrictions, diagnoses, medications, swallowingStatus, oralHealth, dailyIntake, fluidIntake, supplements, eatingAssistance, shopping, mealPrep, financialStatus',
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Emergency Response Template
INSERT INTO prompt_templates (id, name, description, category, template, medical_context, safety_guidelines, expected_variables, version, is_active, created_at, updated_at)
VALUES (
    RANDOM_UUID(),
    'Emergency Situation Response Guide',
    'Template for handling common geriatric emergencies',
    'EMERGENCY',
    'Emergency Response Protocol for ${patientName}

SITUATION: ${emergencyType}
PATIENT STATUS: ${patientStatus}

Critical Information:
- Age: ${age}
- Known conditions: ${conditions}
- Current medications: ${medications}
- Allergies: ${allergies}
- Emergency contacts: ${emergencyContacts}

Immediate Assessment Needed:
1. Vital Signs:
   - Level of consciousness: ${consciousness}
   - Breathing: ${breathing}
   - Circulation: ${circulation}
   - Temperature: ${temperature}

2. Specific Emergency Evaluation:
   ${emergencySpecificAssessment}

IMMEDIATE ACTIONS REQUIRED:
Based on the emergency type, provide:
1. Step-by-step immediate response actions
2. When to call 911 vs. when to contact physician
3. Information to provide to emergency services
4. What NOT to do
5. Comfort measures while waiting for help
6. Documentation needed

SPECIAL CONSIDERATIONS FOR ELDERLY:
- Age-related response to emergency
- Medication effects on presentation
- Communication strategies if cognitively impaired
- Positioning considerations
- Prevention of secondary complications

Follow-up Actions:
- Documentation requirements
- Family notification protocol
- Post-emergency monitoring
- Prevention strategies for future',
    'For guiding appropriate emergency response in geriatric care settings',
    'CRITICAL: In life-threatening emergencies, call 911 immediately. Do not delay emergency services. Provide clear location information. Never leave patient alone if unstable.',
    'patientName, emergencyType, patientStatus, age, conditions, medications, allergies, emergencyContacts, consciousness, breathing, circulation, temperature, emergencySpecificAssessment',
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
