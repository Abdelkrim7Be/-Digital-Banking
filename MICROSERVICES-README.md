# Digital Banking - Microservices Architecture

## Overview

Digital Banking application transformed into a microservices architecture with Spring Cloud components.

## Architecture Components

### Infrastructure Services

1. **Discovery Service (Eureka Server)** - Port 8761
   - Service registry for all microservices
   - Enables dynamic service discovery

2. **Config Service** - Port 8888
   - Centralized configuration management
   - Supports environment-specific configs

3. **Gateway Service (API Gateway)** - Port 8080
   - Single entry point for all client requests
   - Routes requests to appropriate microservices
   - Handles CORS and authentication

### Business Services

4. **Customer Service** - Port 8081
   - Customer and user management
   - Authentication and authorization
   - JWT token generation

5. **Account Service** - Port 8082
   - Bank account management
   - Account creation and retrieval
   - Balance management

6. **Transaction Service** - Port 8083
   - Transaction operations (debit, credit, transfer)
   - Transaction history
   - Account operations

### Frontend

7. **Angular Frontend** - Port 4200
   - User interface for customers and admins
   - Communicates with backend via API Gateway

## Technology Stack

- **Java 21** - Programming language
- **Spring Boot 3.2.1** - Application framework
- **Spring Cloud 2023.0.0** - Microservices framework
- **Netflix Eureka** - Service discovery
- **Spring Cloud Gateway** - API Gateway
- **Spring Cloud Config** - Configuration management
- **Spring Cloud OpenFeign** - Inter-service communication
- **H2 Database** - In-memory database for each service
- **Docker & Docker Compose** - Containerization
- **Angular** - Frontend framework

## Getting Started

### Prerequisites

- Java 21
- Maven 3.6+
- Docker & Docker Compose
- Node.js & npm (for frontend development)

### Build All Microservices

```bash
cd microservices
mvn clean package
```

### Run with Docker Compose

```bash
docker-compose -f docker-compose-microservices.yml up --build
```

### Access Points

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Frontend**: http://localhost:4200
- **Customer Service**: http://localhost:8081
- **Account Service**: http://localhost:8082
- **Transaction Service**: http://localhost:8083

### API Gateway Routes

All client requests go through the API Gateway:

- `http://localhost:8080/api/customers/**` → Customer Service
- `http://localhost:8080/api/accounts/**` → Account Service
- `http://localhost:8080/api/transactions/**` → Transaction Service

## Development

### Running Individual Services

Each service can be run independently:

```bash
cd microservices/discovery-service
mvn spring-boot:run
```

### Service Communication

Services communicate using:
- **Synchronous**: Feign Client for REST calls
- **Service Discovery**: Eureka for dynamic service location

## Migration from Monolith

The monolithic version is preserved in the `Monolithique` branch.

### Key Changes

1. **Database per Service**: Each microservice has its own database
2. **Service Discovery**: Dynamic service registration with Eureka
3. **API Gateway**: Centralized routing and security
4. **Decoupled Services**: Independent deployment and scaling

## Testing

Each service includes H2 console for database inspection:
- Customer Service: http://localhost:8081/h2-console
- Account Service: http://localhost:8082/h2-console
- Transaction Service: http://localhost:8083/h2-console

## Next Steps

### Planned Enhancements

1. **Message Broker** (RabbitMQ/Kafka) for async communication
2. **Distributed Tracing** (Sleuth + Zipkin)
3. **Centralized Logging** (ELK Stack)
4. **Circuit Breaker** (Resilience4j)
5. **API Documentation** (Swagger/OpenAPI)
6. **Monitoring** (Prometheus + Grafana)
7. **Production Database** (PostgreSQL/MySQL per service)

## Project Structure

```
Digital-Banking/
├── microservices/
│   ├── discovery-service/      # Eureka Server
│   ├── config-service/          # Config Server
│   ├── gateway-service/         # API Gateway
│   ├── customer-service/        # Customer management
│   ├── account-service/         # Account management
│   └── transaction-service/     # Transaction operations
├── frontend-angular/            # Angular frontend
├── backend/                     # Legacy monolith (deprecated)
├── docker-compose-microservices.yml
└── README.md
```

## License

This project is for educational purposes.

