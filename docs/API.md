# 📚 API Documentation

## Overview

The Geriatric Home Care Management System provides a comprehensive REST API for managing elderly patient care.

## Interactive Documentation

### Swagger UI
Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

Features:
- ✅ Try out API endpoints directly
- ✅ View request/response schemas
- ✅ See authentication requirements
- ✅ Filter endpoints by module
- ✅ Export OpenAPI specification

### OpenAPI Specification
Download the OpenAPI 3.0 specification:
```
http://localhost:8080/api-docs
```

## API Modules

### 1. Patients API
**Base Path:** `/api/patients`

**Endpoints:**
- `POST /api/patients` - Create new patient
- `GET /api/patients/{id}` - Get patient by ID
- `PUT /api/patients/{id}` - Update patient
- `DELETE /api/patients/{id}` - Soft delete patient
- `GET /api/patients` - List all patients (paginated)
- `GET /api/patients/search` - Search patients

**Required Roles:** ADMIN, PHYSICIAN, NURSE

---

### 2. Care Plans API
**Base Path:** `/api/care-plans`

**Endpoints:**
- `POST /api/care-plans` - Create care plan
- `GET /api/care-plans/{id}` - Get care plan
- `PUT /api/care-plans/{id}` - Update care plan
- `PATCH /api/care-plans/{id}/status` - Update status
- `GET /api/care-plans/patient/{patientId}` - Get patient care plans

**Required Roles:** ADMIN, PHYSICIAN

---

### 3. Vital Signs API
**Base Path:** `/api/vital-signs`

**Endpoints:**
- `POST /api/vital-signs` - Record vital signs
- `GET /api/vital-signs/{id}` - Get vital sign record
- `GET /api/vital-signs/patient/{patientId}` - Get patient vital signs
- `GET /api/vital-signs/patient/{patientId}/latest` - Get latest reading
- `GET /api/vital-signs/patient/{patientId}/trends` - Get trend data

**Required Roles:** ADMIN, PHYSICIAN, NURSE, CAREGIVER

**Supported Vital Sign Types:**
- Blood Pressure (systolic/diastolic)
- Heart Rate
- Temperature
- Oxygen Saturation (SpO2)
- Respiratory Rate

---

### 4. Alerts API
**Base Path:** `/api/alerts`

**Endpoints:**
- `GET /api/alerts` - List all alerts
- `GET /api/alerts/active` - Get active alerts
- `GET /api/alerts/patient/{patientId}` - Get patient alerts
- `PUT /api/alerts/{id}/acknowledge` - Acknowledge alert
- `PUT /api/alerts/{id}/resolve` - Resolve alert

**Required Roles:** ADMIN, PHYSICIAN, NURSE

**Alert Severities:**
- CRITICAL - Requires immediate action
- WARNING - Should be reviewed soon

---

### 5. Reports API
**Base Path:** `/api/reports`

**Care Plan Adherence:**
- `POST /api/reports/adherence` - Generate adherence report
- `GET /api/reports/{id}` - Get report
- `GET /api/reports/patient/{patientId}` - List patient reports
- `GET /api/reports/{id}/export/pdf` - Export as PDF
- `GET /api/reports/{id}/export/csv` - Export as CSV

**Medication Adherence:**
- `POST /api/reports/medication/adherence` - Generate medication report
- `GET /api/reports/medication/{id}` - Get medication report
- `GET /api/reports/medication/patient/{patientId}/statistics` - Get statistics

**Required Roles:** ADMIN, PHYSICIAN, NURSE, CAREGIVER

---

### 6. Dashboard API
**Base Path:** `/api/dashboard`

**Endpoints:**
- `GET /api/dashboard/statistics` - Get health outcome dashboard
- `POST /api/dashboard/statistics` - Get dashboard with filters

**Required Roles:** ADMIN, PHYSICIAN

**Dashboard Metrics:**
- Population statistics
- Vital signs aggregations
- Alert frequency
- Care plan adherence rates
- Medication adherence metrics

---

## Authentication

### JWT Token
Most endpoints require authentication. Include JWT token in header:
```
Authorization: Bearer <token>
```

### User Roles
- **ADMIN** - Full system access
- **PHYSICIAN** - Medical care, all reports
- **NURSE** - Patient care, vital signs, alerts
- **CAREGIVER** - Daily care tasks, basic reports
- **FAMILY** - Read-only patient information

---

## Request/Response Format

### Standard Response
```json
{
  "id": "uuid",
  "data": { },
  "timestamp": "2026-02-09T12:00:00Z"
}
```

### Error Response
```json
{
  "timestamp": "2026-02-09T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/patients"
}
```

---

## Pagination

List endpoints support pagination:
```
GET /api/patients?page=0&size=20
```

**Response:**
```json
{
  "content": [ ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 100,
  "totalPages": 5
}
```

---

## Testing with cURL

### Create Patient
```bash
curl -X POST http://localhost:8080/api/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1945-01-15",
    "email": "john.doe@email.com"
  }'
```

### Record Vital Signs
```bash
curl -X POST http://localhost:8080/api/vital-signs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "patientId": "patient-uuid",
    "bloodPressureSystolic": 120,
    "bloodPressureDiastolic": 80,
    "heartRate": 72,
    "temperature": 36.8,
    "oxygenSaturation": 98
  }'
```

### Generate Report
```bash
curl -X POST http://localhost:8080/api/reports/adherence \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "patientId": "patient-uuid",
    "reportType": "CARE_PLAN_ADHERENCE",
    "timePeriod": "LAST_30_DAYS"
  }'
```

---

## Rate Limits

- Authenticated: 1000 requests/hour
- Unauthenticated: 100 requests/hour

---

## Support

For API support:
- Email: support@geriatriccare.com
- GitHub: https://github.com/eder000000/geriatric-home-care-java/issues

