# 🚀 Quick Start Guide
## Transform Your Monolithic Banking App to Microservices

---

## 📚 Documentation Overview

This project now includes comprehensive documentation for transforming your monolithic banking application into a microservices architecture:

### 📄 Available Documents

1. **MICROSERVICES_ARCHITECTURE_PLAN.md**
   - Complete architecture overview
   - Microservices breakdown
   - Technology stack recommendations
   - Enhancement suggestions
   - Implementation roadmap (24-week plan)
   - Migration strategy

2. **IMPLEMENTATION_GUIDE.md**
   - Step-by-step implementation instructions
   - Code structure and module setup
   - Docker Compose configuration
   - Service extraction guide
   - Testing strategies

3. **ENHANCEMENTS_CODE_EXAMPLES.md**
   - Ready-to-use code snippets
   - Rate limiting implementation
   - Redis caching
   - Circuit breakers
   - Email notifications
   - Audit logging
   - And more...

4. **QUICK_START_GUIDE.md** (This file)
   - Quick reference
   - Getting started checklist
   - Common commands
   - Troubleshooting

---

## ✅ Getting Started Checklist

### Phase 1: Preparation (Day 1)

- [ ] **Review Architecture Plan**
  - Read `MICROSERVICES_ARCHITECTURE_PLAN.md`
  - Understand the proposed architecture
  - Identify your priorities

- [ ] **Set Up Development Environment**
  ```bash
  # Verify installations
  java -version          # Should be Java 21
  mvn -version           # Should be Maven 3.8+
  docker --version       # Docker should be installed
  docker-compose --version
  ```

- [ ] **Create New Repository Structure**
  ```bash
  mkdir digital-banking-microservices
  cd digital-banking-microservices
  # Create parent POM (see IMPLEMENTATION_GUIDE.md)
  ```

- [ ] **Set Up Infrastructure Services**
  - Eureka Server (Service Discovery)
  - Config Server
  - API Gateway

---

## 🎯 Recommended Implementation Order

### Week 1-2: Infrastructure Setup

1. **Create Parent POM**
   ```bash
   # Follow Step 1 in IMPLEMENTATION_GUIDE.md
   ```

2. **Set Up Eureka Server**
   ```bash
   # Follow Step 2 in IMPLEMENTATION_GUIDE.md
   cd service-discovery
   mvn spring-boot:run
   # Verify: http://localhost:8761
   ```

3. **Set Up Config Server**
   ```bash
   # Follow Step 3 in IMPLEMENTATION_GUIDE.md
   cd config-server
   mvn spring-boot:run
   # Verify: http://localhost:8888
   ```

4. **Set Up API Gateway**
   ```bash
   # Follow Step 4 in IMPLEMENTATION_GUIDE.md
   cd api-gateway
   mvn spring-boot:run
   # Verify: http://localhost:8080
   ```

### Week 3-4: Extract Auth Service

1. **Create Auth Service Module**
   ```bash
   # Follow Step 5 in IMPLEMENTATION_GUIDE.md
   cd auth-service
   mvn spring-boot:run
   # Verify: http://localhost:8081
   ```

2. **Test Authentication**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'
   ```

### Week 5-6: Extract Customer Service

1. **Create Customer Service Module**
   ```bash
   # Follow Step 6 in IMPLEMENTATION_GUIDE.md
   cd customer-service
   mvn spring-boot:run
   # Verify: http://localhost:8082
   ```

2. **Test Customer Endpoints**
   ```bash
   # Get JWT token first, then:
   curl -X GET http://localhost:8080/api/customers \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

### Week 7-8: Extract Account Service

1. **Create Account Service Module**
   ```bash
   # Follow Step 7 in IMPLEMENTATION_GUIDE.md
   cd account-service
   mvn spring-boot:run
   # Verify: http://localhost:8083
   ```

### Week 9-10: Extract Transaction Service

1. **Create Transaction Service Module**
   ```bash
   # Follow Step 8 in IMPLEMENTATION_GUIDE.md
   cd transaction-service
   mvn spring-boot:run
   # Verify: http://localhost:8084
   ```

---

## 🐳 Docker Quick Start

### Start All Services with Docker Compose

```bash
# Navigate to project root
cd digital-banking-microservices

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

### Individual Service Commands

```bash
# Build specific service
docker-compose build auth-service

# Start specific service
docker-compose up -d auth-service

# View logs for specific service
docker-compose logs -f auth-service

# Restart service
docker-compose restart auth-service
```

---

## 🔧 Common Commands

### Maven Commands

```bash
# Build all modules
mvn clean install

# Build specific module
cd auth-service && mvn clean install

# Run specific service
cd auth-service && mvn spring-boot:run

# Run tests
mvn test

# Run tests for specific module
cd auth-service && mvn test
```

### Database Commands

```bash
# Connect to MySQL (Auth DB)
mysql -h localhost -P 3307 -u root -p

# Connect to MySQL (Customer DB)
mysql -h localhost -P 3308 -u root -p

# Connect to MySQL (Account DB)
mysql -h localhost -P 3309 -u root -p

# Connect to MySQL (Transaction DB)
mysql -h localhost -P 3310 -u root -p
```

### Service URLs

| Service | Port | URL |
|---------|------|-----|
| Eureka Server | 8761 | http://localhost:8761 |
| Config Server | 8888 | http://localhost:8888 |
| API Gateway | 8080 | http://localhost:8080 |
| Auth Service | 8081 | http://localhost:8081 |
| Customer Service | 8082 | http://localhost:8082 |
| Account Service | 8083 | http://localhost:8083 |
| Transaction Service | 8084 | http://localhost:8084 |
| Reporting Service | 8085 | http://localhost:8085 |
| Notification Service | 8086 | http://localhost:8086 |

---

## 🧪 Testing

### Unit Tests

```bash
# Run all unit tests
mvn test

# Run tests for specific service
cd auth-service && mvn test

# Run with coverage
mvn test jacoco:report
```

### Integration Tests

```bash
# Run integration tests
mvn verify

# Run with Testcontainers
mvn test -Dtest=*IntegrationTest
```

### API Testing

```bash
# Using curl
curl -X GET http://localhost:8080/api/customers \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Using httpie (if installed)
http GET localhost:8080/api/customers \
  Authorization:"Bearer YOUR_JWT_TOKEN"
```

---

## 🐛 Troubleshooting

### Common Issues

#### 1. Service Not Registering with Eureka

**Problem**: Service doesn't appear in Eureka dashboard

**Solution**:
```yaml
# Check application.yml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

#### 2. Connection Refused Errors

**Problem**: Cannot connect to database or other services

**Solution**:
- Check if services are running: `docker ps`
- Check ports: `netstat -tulpn | grep PORT`
- Verify database is accessible
- Check network configuration in docker-compose.yml

#### 3. JWT Token Issues

**Problem**: Authentication fails

**Solution**:
- Verify JWT secret is same across services
- Check token expiration time
- Verify token format: `Bearer <token>`
- Check token in jwt.io for debugging

#### 4. Service Discovery Issues

**Problem**: Services can't find each other

**Solution**:
- Verify Eureka is running
- Check service names match
- Verify network connectivity
- Check logs for registration errors

#### 5. Database Connection Issues

**Problem**: Cannot connect to database

**Solution**:
```bash
# Check if database is running
docker ps | grep mysql

# Check database logs
docker logs mysql-auth

# Verify connection string
# Check application.yml datasource configuration
```

---

## 📊 Monitoring

### Health Checks

```bash
# Check service health
curl http://localhost:8081/actuator/health

# Check all actuator endpoints
curl http://localhost:8081/actuator

# Check metrics
curl http://localhost:8081/actuator/metrics
```

### Eureka Dashboard

Access: http://localhost:8761

- View registered services
- Check service status
- Monitor service instances

---

## 🔐 Security Best Practices

1. **Environment Variables**
   ```bash
   # Use environment variables for secrets
   export JWT_SECRET=your-secret-key
   export DB_PASSWORD=your-db-password
   ```

2. **API Gateway Security**
   - Implement rate limiting
   - Add CORS configuration
   - Use HTTPS in production

3. **Service-to-Service Communication**
   - Use API keys or mTLS
   - Implement circuit breakers
   - Add retry logic

---

## 📈 Performance Optimization

### 1. Enable Caching

```java
// Add @Cacheable annotation
@Cacheable(value = "customers", key = "#id")
public CustomerDTO getCustomer(Long id) {
    // ...
}
```

### 2. Database Connection Pooling

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

### 3. Response Compression

```yaml
server:
  compression:
    enabled: true
    mime-types: application/json,application/xml
    min-response-size: 1024
```

---

## 🎓 Learning Resources

### Spring Cloud Documentation
- https://spring.io/projects/spring-cloud
- https://spring.io/guides

### Microservices Patterns
- "Microservices Patterns" by Chris Richardson
- "Building Microservices" by Sam Newman

### Tools
- Postman (API testing)
- Docker Desktop
- IntelliJ IDEA (IDE)
- Grafana & Prometheus (Monitoring)

---

## 📞 Next Steps

1. **Review Documentation**
   - Read `MICROSERVICES_ARCHITECTURE_PLAN.md` thoroughly
   - Understand the architecture
   - Plan your implementation

2. **Start Small**
   - Begin with infrastructure setup
   - Extract one service at a time
   - Test thoroughly after each extraction

3. **Iterate**
   - Don't try to do everything at once
   - Follow the roadmap
   - Adjust based on your needs

4. **Monitor & Improve**
   - Set up monitoring early
   - Collect metrics
   - Optimize based on data

---

## ✅ Success Criteria

You'll know you're on the right track when:

- ✅ All services register with Eureka
- ✅ API Gateway routes requests correctly
- ✅ Services communicate via Feign clients
- ✅ Authentication works across services
- ✅ Database per service is working
- ✅ Docker Compose starts all services
- ✅ Health checks pass
- ✅ Tests are passing

---

## 🎯 Quick Reference

### Start Development Environment

```bash
# 1. Start Eureka
cd service-discovery && mvn spring-boot:run

# 2. Start Config Server (new terminal)
cd config-server && mvn spring-boot:run

# 3. Start API Gateway (new terminal)
cd api-gateway && mvn spring-boot:run

# 4. Start Services (new terminals)
cd auth-service && mvn spring-boot:run
cd customer-service && mvn spring-boot:run
# ... etc
```

### Or Use Docker Compose

```bash
docker-compose up -d
```

### Verify Everything is Running

```bash
# Check Eureka
curl http://localhost:8761

# Check API Gateway
curl http://localhost:8080/actuator/health

# Check Services
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

---

**Good luck with your microservices transformation! 🚀**

Remember: Start small, test often, and iterate based on feedback.


