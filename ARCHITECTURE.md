# Digital Banking Microservices Architecture

## 🏛️ System Architecture

### High-Level Overview

The Digital Banking system is built using a microservices architecture pattern, where each service is responsible for a specific business domain. All services communicate through an API Gateway and are registered with a service discovery mechanism.

## 🔄 Request Flow

```
Client Request
    ↓
API Gateway (JWT Validation, Rate Limiting)
    ↓
Service Discovery (Eureka)
    ↓
Target Microservice
    ↓
Database (H2 - In-Memory)
```

## 📊 Service Communication

### Synchronous Communication
- **Feign Clients**: Used for REST API calls between services
- **Circuit Breakers**: Resilience4j for fault tolerance
- **Retry Logic**: Exponential backoff for transient failures

### Service Dependencies

```
Customer Service
    ↓ (no dependencies)

Account Service
    ↓ (depends on)
Customer Service (validates customer exists)

Transaction Service
    ↓ (depends on)
Account Service (gets/updates balance)
Customer Service (optional, for validation)

Reporting Service
    ↓ (depends on)
Customer Service (aggregates customer data)
Account Service (aggregates account data)
Transaction Service (aggregates transaction data)

Notification Service
    ↓ (no dependencies, called by other services)
```

## 🗄️ Data Management

### Database Strategy
- **Current**: H2 In-Memory Database (per service)
- **Production**: PostgreSQL/MySQL (recommended)
- **Data Isolation**: Each service has its own database

### Data Flow

```
Customer Service DB
    ├── Users
    └── Customers

Account Service DB
    ├── BankAccounts
    ├── CurrentAccounts
    └── SavingAccounts

Transaction Service DB
    └── AccountOperations

Notification Service DB
    └── Notifications
```

## 🔐 Security Architecture

### Authentication Flow

```
1. User Registration/Login
   ↓
2. Customer Service generates JWT
   ↓
3. Client stores JWT
   ↓
4. Subsequent requests include JWT in Authorization header
   ↓
5. API Gateway validates JWT
   ↓
6. Request forwarded to target service
```

### Security Layers

1. **API Gateway Layer**
   - JWT validation
   - Rate limiting
   - CORS handling

2. **Service Layer**
   - Spring Security
   - Role-based access (future)

3. **Data Layer**
   - Database-level security
   - Encrypted connections

## 🛡️ Resilience Patterns

### Circuit Breaker Pattern
- **Purpose**: Prevent cascading failures
- **Implementation**: Resilience4j
- **Configuration**: 
  - Failure threshold: 50%
  - Wait duration: 5 seconds
  - Sliding window: 10 calls

### Retry Pattern
- **Purpose**: Handle transient failures
- **Implementation**: Exponential backoff
- **Configuration**:
  - Max attempts: 3
  - Base delay: 1 second
  - Multiplier: 2x

### Fallback Pattern
- **Purpose**: Graceful degradation
- **Implementation**: Feign fallback classes
- **Behavior**: Return empty/default data when service unavailable

## 📈 Observability

### Distributed Tracing
- **Tool**: Zipkin
- **Purpose**: Track requests across services
- **Sampling**: 100% (configurable)

### Metrics
- **Tool**: Prometheus + Micrometer
- **Endpoints**: `/actuator/prometheus`
- **Metrics**: Request counts, latency, errors

### Logging
- **Format**: Structured logging
- **Level**: INFO (configurable)
- **Correlation**: Trace IDs in logs

## 🚀 Deployment Architecture

### Container Strategy
- **One container per service**
- **Docker Compose for orchestration**
- **Service mesh ready** (future: Istio/Linkerd)

### Network Architecture

```
Docker Network: microservices-network
    ├── discovery-service
    ├── config-service
    ├── gateway-service
    ├── customer-service
    ├── account-service
    ├── transaction-service
    ├── reporting-service
    ├── notification-service
    └── zipkin
```

## 🔄 Scalability Considerations

### Horizontal Scaling
- Services can be scaled independently
- Load balancing via Eureka
- Stateless services (except databases)

### Vertical Scaling
- JVM tuning per service
- Resource limits in Docker
- Database connection pooling

## 📋 Service Responsibilities

### Customer Service
- **Domain**: User and Customer Management
- **Responsibilities**:
  - User registration and authentication
  - Customer CRUD operations
  - JWT token generation
  - Profile management

### Account Service
- **Domain**: Account Management
- **Responsibilities**:
  - Account creation (Current/Saving)
  - Account retrieval and updates
  - Balance management
  - Account status management

### Transaction Service
- **Domain**: Transaction Processing
- **Responsibilities**:
  - Debit/Credit operations
  - Transfer between accounts
  - Transaction history
  - Balance validation

### Reporting Service
- **Domain**: Analytics and Reporting
- **Responsibilities**:
  - Dashboard statistics
  - Report generation
  - Data aggregation
  - Analytics

### Notification Service
- **Domain**: Notifications
- **Responsibilities**:
  - Email notifications
  - SMS notifications
  - Notification history
  - Async processing

## 🔮 Future Enhancements

### Event-Driven Architecture
- Message queue (RabbitMQ/Kafka)
- Event sourcing
- CQRS pattern

### API Gateway Enhancements
- API versioning
- Request/Response transformation
- API analytics

### Security Enhancements
- OAuth2/OIDC
- API key management
- Rate limiting per user

### Monitoring Enhancements
- Grafana dashboards
- Alerting (Prometheus Alertmanager)
- Log aggregation (ELK stack)

