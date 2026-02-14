# Postman Collection - Geriatric Home Care System

## Setup

1. **Import Collection:**
   - Open Postman
   - Click "Import"
   - Select `geriatric-care-collection.json`

2. **Import Environment:**
   - Click "Environments"
   - Import `environments/local.json`
   - Set as active environment

3. **Workflow:**
   - Run "Register User" or "Login" first
   - Token auto-saves to environment
   - All other requests use the token automatically

## Available Collections

### Authentication Flow
- Register new user
- Login (get JWT token)
- Get current user info
- Refresh token
- Check email availability

### Patient Management
- Create patient
- Get patient by ID
- List all patients (paginated)
- Update patient
- Delete patient

### Care Plan Management
- Create care plan
- Get care plan by ID
- List care plans
- Update care plan
- Delete care plan

### Alert Management
- Create alert
- Get alerts for patient
- Acknowledge alert
- List all alerts

### Dashboard & Reports
- Get dashboard statistics
- Generate adherence report
- Get patient statistics

## Environment Variables

- `baseUrl`: http://localhost:8080
- `token`: Auto-populated after login
- `userId`: Auto-populated after login/register
- `patientId`: Set manually or from create response

## Testing Strategy

### Phase 1: Authentication
1. Register new user → Save token
2. Login with credentials → Verify token
3. Get current user → Verify user info

### Phase 2: Core CRUD
1. Create patient → Save patientId
2. Get patient by ID
3. Update patient
4. List patients

### Phase 3: Complex Workflows
1. Create care plan for patient
2. Create alert
3. Generate reports

### Phase 4: Edge Cases
1. Invalid credentials (401)
2. Non-existent resources (404)
3. Invalid data (400)
4. Unauthorized access (403)
