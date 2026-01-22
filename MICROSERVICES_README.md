# Digital Banking - Microservices Architecture

## 🏗️ Architecture Overview

This project is a complete microservices implementation of a Digital Banking system, transformed from a monolithic application into a distributed architecture using Spring Cloud.

### Architecture Diagram

```
┌─────────────┐
│   Client    │
│  (Browser)  │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│      API Gateway (8080)              │
│  - JWT Authentication                │
│  - Rate Limiting                    │
│  - Request Routing                  │
└──────┬──────────────────────────────┘
       │
       ├─────────────────────────────────┐
       │                                 │
       ▼                                 ▼
┌──────────────┐              ┌──────────────────┐
│  Discovery   │              │   Config Service │
│  Service     │              │     (8888)       │
│   (8761)     │              └──────────────────┘
└──────────────┘
       │
       ├──────────────┬──────────────┬──────────────┬──────────────┬──────────────┐
       │              │              │              │              │              │
       ▼              ▼              ▼              ▼              ▼              ▼
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│Customer │  │ Account  │  │Transaction│ │Reporting │  │Notification│ │  Zipkin  │
│Service  │  │ Service  │  │ Service   │ │ Service  │  │ Service   │ │  (9411)   │
│ (8081)  │  │ (8082)   │  │  (8083)   │ │ (8084)   │  │  (8085)   │ │           │
└──────────┘  └──────────┘  └──────────┘ └──────────┘  └──────────┘ └──────────┘
```

## 📦 Services

### Infrastructure Services

#### 1. Discovery Service (Eureka Server) - Port 8761
- Service registry for all microservices
- Enables dynamic service discovery
- Health monitoring

#### 2. Config Service - Port 8888
- Centralized configuration management
- Environment-specific configurations
- Dynamic configuration updates

#### 3. Gateway Service (API Gateway) - Port 8080
- Single entry point for all client requests
- JWT authentication and validation
- Rate limiting (100 requests/minute)
- Request routing to microservices
- CORS handling

### Business Services

#### 4. Customer Service - Port 8081
- User registration and authentication
- Customer management (CRUD)
- JWT token generation
- User profile management

#### 5. Account Service - Port 8082
- Bank account creation (Current/Saving)
- Account management
- Balance management
- Account status updates

#### 6. Transaction Service - Port 8083
- Transaction operations (debit, credit, transfer)
- Transaction history
- Account operations tracking
- Balance validation

#### 7. Reporting Service - Port 8084
- Dashboard statistics
- Customer summary reports
- Account balance analysis
- Transaction analysis reports

#### 8. Notification Service - Port 8085
- Email notifications (SMTP)
- SMS notifications (Twilio ready)
- Notification history
- Async notification processing

### Observability

#### Zipkin - Port 9411
- Distributed tracing
- Request flow visualization
- Performance monitoring

## 🚀 Quick Start

### Prerequisites
- Java 21
- Docker & Docker Compose
- Maven 3.6+ (optional, for local builds)

### Option 1: Docker Compose (Recommended)

```bash
# Start all services
docker-compose -f docker-compose-microservices.yml up --build

# Start in detached mode
docker-compose -f docker-compose-microservices.yml up -d --build

# View logs
docker-compose -f docker-compose-microservices.yml logs -f

# Stop all services
docker-compose -f docker-compose-microservices.yml down
```

### Option 2: Local Development

```bash
# Build all services
cd microservices
mvn clean package -DskipTests

# Start services in order:
# 1. Discovery Service
# 2. Config Service
# 3. Gateway Service
# 4. Business Services (Customer, Account, Transaction, etc.)
```

## 🔐 Authentication Flow

1. **Register User**
   ```bash
   POST http://localhost:8080/api/auth/register
   {
     "username": "user123",
     "email": "user@example.com",
     "password": "password123",
     "name": "John Doe"
   }
   ```

2. **Login**
   ```bash
   POST http://localhost:8080/api/auth/login
   {
     "username": "user123",
     "password": "password123"
   }
   ```
   Returns JWT token

3. **Use Token**
   ```bash
   GET http://localhost:8080/api/customers
   Authorization: Bearer <JWT_TOKEN>
   ```

## 📡 API Endpoints

### Public Endpoints (No JWT Required)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login

### Protected Endpoints (JWT Required)

#### Customer Service
- `GET /api/customers` - Get all customers
- `GET /api/customers/{id}` - Get customer by ID
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Delete customer

#### Account Service
- `GET /api/accounts` - Get all accounts
- `GET /api/accounts/{id}` - Get account by ID
- `POST /api/accounts` - Create account
- `PATCH /api/accounts/{id}/status` - Update account status
- `PATCH /api/accounts/{id}/balance` - Update balance

#### Transaction Service
- `POST /api/transactions/credit` - Credit transaction
- `POST /api/transactions/debit` - Debit transaction
- `POST /api/transactions/transfer` - Transfer between accounts
- `GET /api/transactions/account/{accountId}` - Get transaction history

#### Reporting Service
- `GET /api/reports/dashboard` - Dashboard statistics
- `GET /api/reports/customer-summary` - Customer summary report
- `GET /api/reports/account-balance` - Account balance report
- `GET /api/reports/transaction-analysis?days=30` - Transaction analysis

#### Notification Service
- `POST /api/notifications` - Send notification
- `GET /api/notifications/recipient/{email}` - Get notification history

## 🛡️ Security Features

- **JWT Authentication**: Token-based authentication
- **Rate Limiting**: 100 requests/minute per client
- **CORS**: Configured for frontend access
- **Public Endpoints**: Auth endpoints excluded from JWT validation

## 🔄 Resilience Features

- **Circuit Breakers**: Resilience4j with configurable thresholds
- **Retry Logic**: Exponential backoff retry mechanism
- **Fallback Handlers**: Graceful degradation when services are down
- **Service Discovery**: Dynamic service registration and discovery

## 📊 Observability

### Distributed Tracing
- Access Zipkin UI: http://localhost:9411
- View request traces across services
- Analyze performance bottlenecks

### Metrics
- Prometheus metrics: `/actuator/prometheus`
- Health checks: `/actuator/health`
- Service info: `/actuator/info`

## 🔧 Configuration

### Environment Variables

```bash
# Email Configuration (Notification Service)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# SMS Configuration (Optional)
SMS_FROM=+1234567890
```

### Service Configuration

Each service has its own `application.properties`:
- Database configuration (H2 in-memory)
- Eureka registration
- Tracing configuration
- Resilience4j settings

## 🧪 Testing

### Manual Testing

```bash
# 1. Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"password","name":"Test User"}'

# 2. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"password"}'

# 3. Use token for protected endpoints
TOKEN="<your-jwt-token>"
curl -X GET http://localhost:8080/api/customers \
  -H "Authorization: Bearer $TOKEN"
```

### Service Health Checks

```bash
# Discovery Service
curl http://localhost:8761

# Gateway Health
curl http://localhost:8080/actuator/health

# Customer Service (via Gateway)
curl http://localhost:8080/api/customers
```

## 📁 Project Structure

```
microservices/
├── discovery-service/      # Eureka Server
├── config-service/         # Config Server
├── gateway-service/        # API Gateway
├── customer-service/       # Customer & Auth
├── account-service/        # Account Management
├── transaction-service/    # Transactions
├── reporting-service/      # Analytics & Reports
├── notification-service/   # Notifications
└── pom.xml                 # Parent POM
```

## 🐛 Troubleshooting

### Services Not Starting
1. Check if Discovery Service is running first
2. Verify ports are not in use
3. Check Docker logs: `docker-compose logs <service-name>`

### JWT Authentication Issues
1. Verify JWT secret matches across services
2. Check token expiration time
3. Ensure Authorization header format: `Bearer <token>`

### Service Discovery Issues
1. Verify Eureka registration: http://localhost:8761
2. Check service names match in configuration
3. Ensure services are on the same network

## 📚 Additional Resources

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Eureka Documentation](https://github.com/Netflix/eureka)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Zipkin Documentation](https://zipkin.io/)

## 🎯 Next Steps

1. **Production Deployment**
   - Replace H2 with PostgreSQL/MySQL
   - Configure external configuration server
   - Set up proper logging (ELK stack)
   - Add monitoring (Prometheus + Grafana)

2. **Enhancements**
   - Add API documentation (Swagger/OpenAPI)
   - Implement caching (Redis)
   - Add message queue (RabbitMQ/Kafka)
   - Implement event-driven architecture

3. **Security**
   - Add OAuth2 support
   - Implement RBAC (Role-Based Access Control)
   - Add API versioning
   - Set up WAF (Web Application Firewall)

## 📝 License

This project is part of the Digital Banking application.

