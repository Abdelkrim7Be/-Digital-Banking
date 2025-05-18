# Digital Banking Application

A comprehensive Spring Boot application for digital banking operations.

## Features

- Customer Management (CRUD operations)
- Account Management (Current and Saving accounts)
- Banking Operations (deposit, withdrawal, transfer)
- Interest calculation for Saving accounts
- Account history and statements
- API documentation with OpenAPI/Swagger

## Technology Stack

- Java 21
- Spring Boot 3.4.5
- Spring Data JPA
- Spring Security
- MySQL / H2 Database
- Lombok
- OpenAPI/Swagger

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven
- MySQL (optional, can use H2 in-memory database)

### Running the Application

1. Clone the repository
2. Configure the database in `application.properties`
3. Run the application: `mvn spring-boot:run`
4. Access the API at: `http://localhost:8085`
5. Access the Swagger UI at: `http://localhost:8085/swagger-ui.html`

## API Endpoints

### Customer Management

- `GET /customers` - Get all customers
- `GET /customers/{id}` - Get customer by ID
- `POST /customers` - Create new customer
- `PUT /customers/{id}` - Update customer
- `DELETE /customers/{id}` - Delete customer

### Account Management

- `GET /accounts` - Get all accounts
- `GET /accounts/{id}` - Get account by ID
- `POST /accounts/current` - Create current account
- `POST /accounts/saving` - Create saving account

### Banking Operations

- `POST /accounts/{id}/debit` - Withdraw from account
- `POST /accounts/{id}/credit` - Deposit to account
- `POST /accounts/{id}/apply-interest` - Apply interest (saving accounts)
- `GET /accounts/{id}/operations` - Get account operations
- `GET /accounts/{id}/pageOperations` - Get paginated account operations
