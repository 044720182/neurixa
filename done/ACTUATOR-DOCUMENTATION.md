# Spring Boot Actuator Documentation

## Overview

Spring Boot Actuator has been added to the Neurixa project to provide production-ready monitoring and management capabilities.

---

## Available Endpoints

### Public Endpoints (No Authentication Required)

#### 1. Health Check
**Endpoint:** `GET /actuator/health`

**Access:** Public

**Response:**
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 494384795648,
        "free": 336156909568,
        "threshold": 10485760,
        "path": "/Users/yusuf.ibrahim/Projects/neurixa/.",
        "exists": true
      }
    },
    "mongo": {
      "status": "UP",
      "details": {
        "maxWireVersion": 21
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.4.7"
      }
    },
    "livenessState": {
      "status": "UP"
    },
    "readinessState": {
      "status": "UP"
    }
  }
}
```

**Usage:**
```bash
curl http://localhost:8080/actuator/health
```

**Health Indicators:**
- ✅ Disk Space
- ✅ MongoDB Connection
- ✅ Redis Connection
- ✅ Liveness Probe
- ✅ Readiness Probe

---

#### 2. Application Info
**Endpoint:** `GET /actuator/info`

**Access:** Public

**Response:**
```json
{}
```

**Note:** Currently empty. Can be configured with application metadata.

---

### Protected Endpoints (Requires ADMIN Role)

The following endpoints require JWT authentication with ROLE_ADMIN:

#### 1. Metrics
**Endpoint:** `GET /actuator/metrics`

**Access:** ADMIN only

**Lists all available metrics:**
- JVM metrics
- System metrics
- Application metrics
- MongoDB metrics
- Redis metrics
- HTTP metrics

**Usage:**
```bash
# Get admin token first
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

# Access metrics
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/actuator/metrics
```

---

#### 2. Prometheus Metrics
**Endpoint:** `GET /actuator/prometheus`

**Access:** ADMIN only

**Provides metrics in Prometheus format for scraping.**

---

#### 3. Environment
**Endpoint:** `GET /actuator/env`

**Access:** ADMIN only (dev profile only)

**Shows environment properties and configuration.**

---

#### 4. Beans
**Endpoint:** `GET /actuator/beans`

**Access:** ADMIN only (dev profile only)

**Lists all Spring beans in the application context.**

---

## Security Configuration

### Security Filter Chains

The application uses three security filter chains:

#### 1. Admin Chain (Order 1)
```java
/admin/** → Requires ROLE_ADMIN
```

#### 2. Actuator Chain (Order 2)
```java
/actuator/health → Public
/actuator/info → Public
/actuator/** → Requires ROLE_ADMIN
```

#### 3. API Chain (Order 3)
```java
/api/auth/** → Public
/api/** → Requires Authentication
```

---

## Configuration

### application.yml (Production)
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  health:
    mongo:
      enabled: true
    redis:
      enabled: true
```

**Production Settings:**
- Health details shown only when authorized
- Limited endpoints exposed
- Kubernetes probes enabled

---

### application-dev.yml (Development)
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,env,beans
  endpoint:
    health:
      show-details: always
```

**Development Settings:**
- Health details always shown
- Additional endpoints exposed (env, beans)
- More verbose for debugging

---

## Health Checks

### Liveness Probe
**Endpoint:** `GET /actuator/health/liveness`

**Purpose:** Indicates if the application is running

**Kubernetes Usage:**
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
```

---

### Readiness Probe
**Endpoint:** `GET /actuator/health/readiness`

**Purpose:** Indicates if the application is ready to accept traffic

**Kubernetes Usage:**
```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
```

---

## Metrics Available

### JVM Metrics
- Memory usage (heap, non-heap)
- Garbage collection
- Thread count
- Class loading

### System Metrics
- CPU usage
- File descriptors
- Uptime

### Application Metrics
- HTTP requests (count, duration)
- Active sessions
- Exception counts

### Database Metrics
- MongoDB connection pool
- MongoDB command execution
- Redis connection pool

### Custom Metrics
Can be added using Micrometer:
```java
@Component
public class CustomMetrics {
    private final Counter userRegistrations;
    
    public CustomMetrics(MeterRegistry registry) {
        this.userRegistrations = Counter.builder("user.registrations")
            .description("Total user registrations")
            .register(registry);
    }
}
```

---

## Testing Actuator Endpoints

### 1. Test Health (Public)
```bash
curl http://localhost:8080/actuator/health
```

**Expected:** 200 OK with health status

---

### 2. Test Info (Public)
```bash
curl http://localhost:8080/actuator/info
```

**Expected:** 200 OK with application info

---

### 3. Test Metrics (Protected)
```bash
# Without token
curl http://localhost:8080/actuator/metrics

# Expected: 401 Unauthorized

# With admin token
TOKEN="<admin-jwt-token>"
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/actuator/metrics

# Expected: 200 OK with metrics list
```

---

## Monitoring Integration

### Prometheus
Add to `prometheus.yml`:
```yaml
scrape_configs:
  - job_name: 'neurixa'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
    bearer_token: '<admin-jwt-token>'
```

### Grafana
1. Add Prometheus as data source
2. Import Spring Boot dashboard
3. Configure alerts

### ELK Stack
Configure Logstash to parse actuator metrics:
```conf
input {
  http_poller {
    urls => {
      neurixa => {
        url => "http://localhost:8080/actuator/metrics"
        headers => {
          Authorization => "Bearer <admin-jwt-token>"
        }
      }
    }
    schedule => { every => "30s" }
  }
}
```

---

## Adding Custom Info

### application.yml
```yaml
info:
  app:
    name: Neurixa
    description: Multi-module Spring Boot Application
    version: 1.0.0
  company:
    name: Your Company
  build:
    artifact: neurixa-boot
    version: 1.0.0
```

### Java Configuration
```java
@Component
public class InfoContributor implements org.springframework.boot.actuate.info.InfoContributor {
    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("custom", Map.of(
            "key", "value",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
```

---

## Security Best Practices

### Production Recommendations

1. **Limit Exposed Endpoints**
   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: health,info,metrics,prometheus
   ```

2. **Require Authentication**
   - Health and info can be public
   - All other endpoints require ADMIN role

3. **Use HTTPS**
   - Never expose actuator over HTTP in production

4. **Rotate JWT Secrets**
   - Admin tokens should have short expiration
   - Rotate secrets regularly

5. **Monitor Access**
   - Log all actuator endpoint access
   - Alert on suspicious patterns

6. **Network Isolation**
   - Consider separate management port
   - Use firewall rules to restrict access

---

## Troubleshooting

### Health Check Fails

**Problem:** `/actuator/health` returns DOWN

**Check:**
1. MongoDB connection: `db.users.find()`
2. Redis connection: `redis-cli ping`
3. Disk space: `df -h`

---

### Metrics Not Available

**Problem:** `/actuator/metrics` returns 401

**Solution:**
1. Ensure you have ADMIN role
2. Include JWT token in Authorization header
3. Check token expiration

---

### Endpoint Not Found

**Problem:** `/actuator/xyz` returns 404

**Check:**
1. Endpoint is included in `management.endpoints.web.exposure.include`
2. Endpoint is available in your Spring Boot version
3. Required dependency is added

---

## Summary

| Endpoint | Access | Purpose |
|----------|--------|---------|
| /actuator/health | Public | Application health status |
| /actuator/info | Public | Application information |
| /actuator/metrics | ADMIN | Application metrics |
| /actuator/prometheus | ADMIN | Prometheus metrics |
| /actuator/env | ADMIN (dev) | Environment properties |
| /actuator/beans | ADMIN (dev) | Spring beans |

**Status:** ✅ Actuator Fully Configured

The application now has production-ready monitoring and management capabilities.
