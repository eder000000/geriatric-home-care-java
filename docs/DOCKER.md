# 🐳 Docker Deployment Guide

## Overview

This guide covers deploying the Geriatric Home Care System using Docker and Docker Compose.

---

## Prerequisites

- Docker 20.10+
- Docker Compose 2.0+
- 4GB RAM minimum
- 10GB disk space

---

## Quick Start (Development)

### 1. Start the Application
```bash
# Using helper script
./scripts/docker-dev-start.sh

# Or manually
docker-compose up --build -d
```

### 2. Access the Application

- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Database**: localhost:5432

### 3. View Logs
```bash
# All containers
docker-compose logs -f

# Just the app
docker-compose logs -f app

# Just the database
docker-compose logs -f postgres
```

### 4. Stop the Application
```bash
# Using helper script
./scripts/docker-stop.sh

# Or manually
docker-compose down
```

---

## Production Deployment

### 1. Create Environment File
```bash
cp .env.example .env
# Edit .env and update all passwords and secrets
```

### 2. Start Production Environment
```bash
# Using helper script
./scripts/docker-prod-start.sh

# Or manually
docker-compose -f docker-compose.prod.yml up --build -d
```

### 3. Verify Deployment
```bash
# Check container health
docker-compose -f docker-compose.prod.yml ps

# Check application health
curl http://localhost:8080/actuator/health
```

---

## Container Management

### Build Image
```bash
docker build -t geriatric-care:latest .
```

### Run Standalone Container
```bash
docker run -d \
  --name geriatric-care-app \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/db \
  -e SPRING_DATASOURCE_USERNAME=user \
  -e SPRING_DATASOURCE_PASSWORD=pass \
  geriatric-care:latest
```

### Shell Access
```bash
# App container
docker-compose exec app sh

# Database container
docker-compose exec postgres psql -U geriatric_user -d geriatric_care
```

---

## Database Management

### Backup Database
```bash
docker-compose exec postgres pg_dump \
  -U geriatric_user geriatric_care > backup.sql
```

### Restore Database
```bash
cat backup.sql | docker-compose exec -T postgres psql \
  -U geriatric_user geriatric_care
```

### Access Database
```bash
docker-compose exec postgres psql -U geriatric_user -d geriatric_care
```

---

## Troubleshooting

### Container Won't Start
```bash
# Check logs
docker-compose logs app

# Check if port is in use
lsof -i :8080

# Remove containers and try again
docker-compose down -v
docker-compose up --build
```

### Database Connection Issues
```bash
# Check database health
docker-compose exec postgres pg_isready

# Check environment variables
docker-compose exec app env | grep SPRING_DATASOURCE
```

### Application Health Check Failing
```bash
# Check application logs
docker-compose logs -f app

# Check health endpoint
curl http://localhost:8080/actuator/health
```

---

## Performance Tuning

### Resource Limits

Edit `docker-compose.prod.yml`:
```yaml
deploy:
  resources:
    limits:
      cpus: '4'
      memory: 4G
    reservations:
      cpus: '2'
      memory: 2G
```

### JVM Options

Set in docker-compose:
```yaml
environment:
  JAVA_OPTS: "-Xmx2048m -Xms1024m -XX:+UseG1GC"
```

---

## Monitoring

### Container Stats
```bash
docker stats geriatric-care-app geriatric-care-db
```

### Application Metrics

Access Spring Boot Actuator endpoints:
- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Info: http://localhost:8080/actuator/info

---

## Scaling

### Horizontal Scaling
```bash
docker-compose up --scale app=3
```

### Load Balancer (Nginx)

Add to `docker-compose.yml`:
```yaml
nginx:
  image: nginx:alpine
  ports:
    - "80:80"
  volumes:
    - ./nginx.conf:/etc/nginx/nginx.conf
  depends_on:
    - app
```

---

## Security

### Best Practices

1. ✅ Always change default passwords
2. ✅ Use secrets management for production
3. ✅ Run as non-root user (already configured)
4. ✅ Keep images updated
5. ✅ Use private registry for production
6. ✅ Enable Docker Content Trust

### Scan for Vulnerabilities
```bash
docker scan geriatric-care:latest
```

---

## CI/CD Integration

### GitHub Actions Example
```yaml
- name: Build Docker Image
  run: docker build -t geriatric-care:${{ github.sha }} .

- name: Push to Registry
  run: |
    docker tag geriatric-care:${{ github.sha }} registry.com/geriatric-care:latest
    docker push registry.com/geriatric-care:latest
```

---

## Support

For Docker-related issues:
- Check logs: `docker-compose logs`
- GitHub Issues: https://github.com/eder000000/geriatric-home-care-java/issues

