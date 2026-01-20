# Digital Banking Application

Full-stack banking solution with Spring Boot backend and Angular frontend.

## Project Structure

```
Digital-Banking/
├── backend/             # Spring Boot REST API
├── frontend/            # Angular application
│   └── assets/         # Screenshots and images
└── docker-compose.yml
```

## Features

- JWT authentication with role-based access
- Customer and account management
- Banking operations (deposit, withdrawal, transfer)
- Transaction history with pagination
- Admin and customer dashboards
- Real-time statistics and charts

## Technology Stack

### Backend
- Java 21
- Spring Boot 3.4.5
- Spring Security + JWT
- Spring Data JPA
- H2/MySQL database
- OpenAPI/Swagger

### Frontend
- Angular 19
- TypeScript
- Bootstrap 5
- Chart.js

## Quick Start with Docker

```bash
docker compose up --build -d
```

Access:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8085
- Swagger UI: http://localhost:8085/swagger-ui.html

## Manual Setup

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm start
```

## Default Credentials

Admin:
- Username: `admin`
- Password: `admin123`

Customer:
- Username: `abdelkrim`
- Password: `password123`

## API Endpoints

Base URL: `http://localhost:8085/api`

### Authentication
- POST `/auth/login` - User login
- POST `/auth/register` - User registration

### Customers
- GET `/customers` - List all customers
- POST `/customers` - Create customer (admin)
- PUT `/customers/{id}` - Update customer (admin)
- DELETE `/customers/{id}` - Delete customer (admin)

### Accounts
- GET `/accounts` - List all accounts
- POST `/accounts/current` - Create current account
- POST `/accounts/saving` - Create saving account
- GET `/accounts/customer/{id}` - Get customer accounts

### Operations
- POST `/accounts/{id}/credit` - Deposit
- POST `/accounts/{id}/debit` - Withdraw
- POST `/accounts/transfer` - Transfer funds
- GET `/accounts/{id}/operations` - Transaction history

## Screenshots

### Admin Interface
![Admin Dashboard](frontend/assets/admin/dashboard%20admin.jpeg)
![Customer Management](frontend/assets/admin/Admin%20Customer%20view.jpeg)
![Account Management](frontend/assets/admin/Accounts%20view%20admin.jpeg)

### Customer Interface
![Customer Dashboard](frontend/assets/customer/customer%20dashboard.jpeg)
![Customer Accounts](frontend/assets/customer/accounts%20view%20customer.jpeg)
![Transactions](frontend/assets/customer/Transactions%20customer.jpeg)

## License

MIT

