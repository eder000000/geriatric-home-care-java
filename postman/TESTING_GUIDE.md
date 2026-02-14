# Postman Testing Guide

## Quick Start (5 minutes)

1. **Start the application:**
```bash
   mvn spring-boot:run
```

2. **Import to Postman:**
   - Open Postman
   - File → Import
   - Select `geriatric-care-collection.json`
   - Import `environments/local.json`

3. **Set environment:**
   - Click environment dropdown (top right)
   - Select "Local Development"

4. **Run authentication flow:**
   - Authentication → Register User
   - Authentication → Login (token auto-saves)
   - Authentication → Get Current User

5. **Test CRUD operations:**
   - Patient Management → Create Patient
   - Patient Management → Get Patient by ID
   - Patient Management → Update Patient

## Testing Workflows

### Workflow 1: New User Registration
```
1. Register User → Creates account, saves userId
2. Login → Gets JWT token, auto-saves to environment
3. Get Current User → Verifies authentication
4. Create Patient → Tests authenticated endpoint
```

### Workflow 2: Care Plan Creation
```
1. Login (if not already)
2. Create Patient → Saves patientId
3. Create Care Plan → Uses patientId from environment
4. Get Care Plan by ID → Verifies creation
5. List Care Plans → See all plans
```

### Workflow 3: Complete Patient Lifecycle
```
1. Create Patient
2. Create Care Plan for patient
3. Check Patient Alerts
4. Update Patient info
5. Get Dashboard Statistics
```

## Environment Variables

Auto-populated after requests:
- `token` - Set after successful login
- `userId` - Set after register/login
- `patientId` - Set after creating patient
- `carePlanId` - Set after creating care plan

## Expected Responses

### Success Cases
- **201 Created:** POST requests (register, create patient, create care plan)
- **200 OK:** GET, PUT requests
- **204 No Content:** DELETE requests

### Error Cases
- **400 Bad Request:** Invalid data/validation errors
- **401 Unauthorized:** Missing or invalid token
- **403 Forbidden:** Insufficient permissions
- **404 Not Found:** Resource doesn't exist

## Newman (CLI Testing)

Run entire collection from command line:
```bash
# Install Newman
npm install -g newman

# Run collection
newman run geriatric-care-collection.json \
  -e environments/local.json \
  --reporters cli,json \
  --reporter-json-export results.json

# Run specific folder
newman run geriatric-care-collection.json \
  -e environments/local.json \
  --folder "Authentication"
```

## CI/CD Integration

Add to GitHub Actions:
```yaml
- name: Run API Tests
  run: |
    npm install -g newman
    newman run postman/geriatric-care-collection.json \
      -e postman/environments/local.json \
      --reporters cli,junit \
      --reporter-junit-export test-results.xml
```

## Tips

1. **Token Expiration:** If getting 401 errors, run Login again
2. **Create Dependencies:** Always create patient before care plan
3. **Clean State:** Delete resources after testing
4. **Check Swagger:** http://localhost:8080/swagger-ui.html for API details
