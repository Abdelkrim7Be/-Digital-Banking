# Digital Banking Application

A comprehensive Spring Boot REST API for digital banking operations with complete Swagger documentation and testing capabilities.

## 🏦 Features

- **Customer Management**: Complete CRUD operations for customer data
- **Account Management**: Support for Current and Saving accounts
- **Banking Operations**: Deposit, withdrawal, and transfer operations
- **Account History**: Complete transaction history with pagination
- **Interest Calculation**: Automatic interest calculation for saving accounts
- **Account Status Management**: Activate, suspend, or close accounts
- **Dashboard & Statistics**: Banking statistics and summary information
- **Comprehensive API Documentation**: Full Swagger/OpenAPI documentation
- **Complete Test Suite**: Unit tests, integration tests, and API testing

## 🛠 Technology Stack

- **Java 21**
- **Spring Boot 3.4.5**
- **Spring Data JPA**
- **H2 Database** (In-memory for development)
- **MySQL** (Production ready)
- **Lombok** (Boilerplate code reduction)
- **OpenAPI/Swagger 3** (API documentation)
- **JUnit 5** (Testing framework)
- **Maven** (Build tool)

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Running the Application

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd dig_bank
   ```

2. **Build the application**

   ```bash
   mvn clean compile
   ```

3. **Run the application**

   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - API Base URL: `http://localhost:8085`
   - Swagger UI: `http://localhost:8085/swagger-ui.html`
   - H2 Console: `http://localhost:8085/h2-console`

## 📚 API Documentation

### Base URL

```
http://localhost:8085/api
```

### Customer Management Endpoints

| Method | Endpoint            | Description                 |
| ------ | ------------------- | --------------------------- |
| GET    | `/customers`        | Get all customers           |
| GET    | `/customers/{id}`   | Get customer by ID          |
| POST   | `/customers`        | Create new customer         |
| PUT    | `/customers/{id}`   | Update customer             |
| DELETE | `/customers/{id}`   | Delete customer             |
| GET    | `/customers/page`   | Get paginated customers     |
| GET    | `/customers/search` | Search customers by keyword |

### Account Management Endpoints

| Method | Endpoint                          | Description            |
| ------ | --------------------------------- | ---------------------- |
| GET    | `/accounts`                       | Get all accounts       |
| GET    | `/accounts/{id}`                  | Get account by ID      |
| GET    | `/accounts/customer/{customerId}` | Get customer accounts  |
| POST   | `/accounts/current`               | Create current account |
| POST   | `/accounts/saving`                | Create saving account  |
| PUT    | `/accounts/{id}/status`           | Update account status  |

### Banking Operations Endpoints

| Method | Endpoint                        | Description                      |
| ------ | ------------------------------- | -------------------------------- |
| POST   | `/accounts/{id}/credit`         | Deposit money                    |
| POST   | `/accounts/{id}/debit`          | Withdraw money                   |
| POST   | `/accounts/transfer`            | Transfer between accounts        |
| POST   | `/accounts/{id}/apply-interest` | Apply interest to saving account |
| GET    | `/accounts/{id}/operations`     | Get account operations           |
| GET    | `/accounts/{id}/history`        | Get paginated account history    |

### Dashboard & Statistics Endpoints

| Method | Endpoint                       | Description            |
| ------ | ------------------------------ | ---------------------- |
| GET    | `/dashboard/stats`             | Get banking statistics |
| GET    | `/dashboard/accounts-summary`  | Get accounts summary   |
| GET    | `/dashboard/customers-summary` | Get customers summary  |
| GET    | `/dashboard/health`            | Health check           |

## 🧪 Testing the API

### 1. Using Swagger UI (Recommended)

1. Start the application
2. Open `http://localhost:8085/swagger-ui.html`
3. Explore and test all endpoints interactively

### 2. Using cURL Commands

#### Create a Customer

```bash
curl -X POST "http://localhost:8085/api/customers" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "1234567890",
    "address": "123 Main Street"
  }'
```

#### Create a Current Account

```bash
curl -X POST "http://localhost:8085/api/accounts/current" \
  -H "Content-Type: application/json" \
  -d "initialBalance=1000&overDraft=500&customerId=1"
```

#### Deposit Money

```bash
curl -X POST "http://localhost:8085/api/accounts/{accountId}/credit" \
  -H "Content-Type: application/json" \
  -d "amount=200&description=Salary deposit"
```

#### Get Account History

```bash
curl -X GET "http://localhost:8085/api/accounts/{accountId}/history?page=0&size=10"
```

### 3. Using Postman

Import the OpenAPI specification from `http://localhost:8085/v3/api-docs` into Postman for a complete collection of API endpoints.

## 🧪 Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Classes

```bash
# Unit tests
mvn test -Dtest=CustomerControllerTest
mvn test -Dtest=BankAccountControllerTest

# Integration tests
mvn test -Dtest=DigitalBankingIntegrationTest
```

### Test Coverage

The project includes:

- **Unit Tests**: Controller layer testing with mocked services
- **Integration Tests**: Full application testing with real database
- **API Tests**: Complete workflow testing

## 🗄 Database Configuration

### MySQL Database Setup

1. **Install MySQL** (if not already installed)
2. **Create Database** (optional - will be created automatically)

   ```sql
   CREATE DATABASE digital_banking;
   ```

3. **Update Configuration** in `application.properties`:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/digital_banking?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

4. **Replace `your_password`** with your actual MySQL root password

### Alternative H2 Configuration (for development/testing)

If you prefer to use H2 in-memory database for development, uncomment the H2 section in `application.properties`:

```properties
# Uncomment these lines and comment out MySQL configuration
# spring.datasource.url=jdbc:h2:mem:digital-bank
# spring.datasource.driver-class-name=org.h2.Driver
# spring.datasource.username=sa
# spring.datasource.password=
# spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
# spring.h2.console.enabled=true
```

## 📊 Sample Data

The application automatically creates sample data on startup:

- 3 sample customers
- Multiple accounts (Current and Saving)
- Sample transactions

## 🔧 Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# Server configuration
server.port=8085

# MySQL Database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/digital_banking?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

# Swagger configuration
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
```

## 🚀 Deployment

### Building for Production

```bash
mvn clean package
java -jar target/dig_bank-0.0.1-SNAPSHOT.jar
```

### Docker Deployment

```dockerfile
FROM openjdk:21-jdk-slim
COPY target/dig_bank-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 📝 License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## 📞 Support

For support and questions:

- Email: dev@digitalbanking.com
- GitHub Issues: [Create an issue](https://github.com/bellagnech/digital-banking/issues)

## 🎯 Next Steps

- [ ] Add authentication and authorization
- [ ] Implement rate limiting
- [ ] Add audit logging
- [ ] Create frontend application
- [ ] Add monitoring and metrics
- [ ] Implement caching
- [ ] Add email notifications

- `POST /accounts/{id}/debit` - Withdraw from account
- `POST /accounts/{id}/credit` - Deposit to account
- `POST /accounts/{id}/apply-interest` - Apply interest (saving accounts)
- `GET /accounts/{id}/operations` - Get account operations
- `GET /accounts/{id}/pageOperations` - Get paginated account operations
