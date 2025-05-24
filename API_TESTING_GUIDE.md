# Digital Banking API Testing Guide

## 🚀 Quick Start

### 1. Setup MySQL Database

Before starting the application, ensure MySQL is running and configured:

```bash
# Start MySQL service (if not already running)
# Windows: Start MySQL service from Services
# macOS: brew services start mysql
# Linux: sudo systemctl start mysql

# Update application.properties with your MySQL password
# Replace 'your_password' with your actual MySQL root password
```

### 2. Start the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8085`

**Note:** The application will automatically create the `digital_banking` database if it doesn't exist.

### 3. Access Swagger UI

Open your browser and navigate to:

```
http://localhost:8085/swagger-ui.html
```

## 📋 Complete API Testing Workflow

### Step 1: Create a Customer

**Endpoint:** `POST /api/customers`

**Request Body:**

```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "1234567890",
  "address": "123 Main Street"
}
```

**Expected Response:** `201 Created`

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "1234567890",
  "address": "123 Main Street"
}
```

### Step 2: Create a Current Account

**Endpoint:** `POST /api/accounts/current`

**Parameters:**

- `initialBalance`: 1000.0
- `overDraft`: 500.0
- `customerId`: 1

**Expected Response:** `201 Created`

```json
{
  "id": "ACC-XXXX",
  "balance": 1000.0,
  "overDraft": 500.0,
  "status": "CREATED",
  "type": "CURRENT",
  "customerDTO": {
    "id": 1,
    "name": "John Doe"
  }
}
```

### Step 3: Create a Saving Account

**Endpoint:** `POST /api/accounts/saving`

**Parameters:**

- `initialBalance`: 5000.0
- `interestRate`: 3.5
- `customerId`: 1

**Expected Response:** `201 Created`

```json
{
  "id": "ACC-YYYY",
  "balance": 5000.0,
  "interestRate": 3.5,
  "status": "CREATED",
  "type": "SAVING",
  "customerDTO": {
    "id": 1,
    "name": "John Doe"
  }
}
```

### Step 4: Perform Banking Operations

#### Deposit Money

**Endpoint:** `POST /api/accounts/{accountId}/credit`

**Parameters:**

- `amount`: 200.0
- `description`: "Salary deposit"

**Expected Response:** `200 OK`

#### Withdraw Money

**Endpoint:** `POST /api/accounts/{accountId}/debit`

**Parameters:**

- `amount`: 150.0
- `description`: "ATM withdrawal"

**Expected Response:** `200 OK`

#### Transfer Money

**Endpoint:** `POST /api/accounts/transfer`

**Parameters:**

- `sourceAccountId`: ACC-YYYY (saving account)
- `destinationAccountId`: ACC-XXXX (current account)
- `amount`: 300.0

**Expected Response:** `200 OK`

### Step 5: Check Account Information

#### Get Account Details

**Endpoint:** `GET /api/accounts/{accountId}`

**Expected Response:** `200 OK`

```json
{
  "id": "ACC-XXXX",
  "balance": 1350.0,
  "status": "CREATED",
  "type": "CURRENT"
}
```

#### Get Account History

**Endpoint:** `GET /api/accounts/{accountId}/operations`

**Expected Response:** `200 OK`

```json
[
  {
    "id": 1,
    "amount": 200.0,
    "description": "Salary deposit",
    "operationDate": "2024-01-01T10:00:00",
    "type": "CREDIT"
  },
  {
    "id": 2,
    "amount": 150.0,
    "description": "ATM withdrawal",
    "operationDate": "2024-01-01T11:00:00",
    "type": "DEBIT"
  }
]
```

#### Get Paginated Account History

**Endpoint:** `GET /api/accounts/{accountId}/history?page=0&size=5`

**Expected Response:** `200 OK`

```json
{
  "accountId": "ACC-XXXX",
  "balance": 1350.0,
  "currentPage": 0,
  "pageSize": 5,
  "totalPages": 1,
  "accountOperationDTOS": [...]
}
```

### Step 6: Customer Management

#### Get All Customers

**Endpoint:** `GET /api/customers`

#### Search Customers

**Endpoint:** `GET /api/customers/search?keyword=John&page=0&size=10`

#### Update Customer

**Endpoint:** `PUT /api/customers/{customerId}`

**Request Body:**

```json
{
  "id": 1,
  "name": "John Updated",
  "email": "john.updated@example.com",
  "phone": "0987654321",
  "address": "456 Oak Street"
}
```

### Step 7: Dashboard & Statistics

#### Get Banking Statistics

**Endpoint:** `GET /api/dashboard/stats`

**Expected Response:** `200 OK`

```json
{
  "totalCustomers": 1,
  "totalAccounts": 2,
  "totalBalance": 6350.0,
  "currentAccounts": 1,
  "savingAccounts": 1,
  "averageBalance": 3175.0
}
```

#### Health Check

**Endpoint:** `GET /api/dashboard/health`

**Expected Response:** `200 OK`

```json
{
  "status": "UP",
  "service": "Digital Banking API",
  "version": "1.0.0",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## 🧪 Testing with cURL

### Create Customer

```bash
curl -X POST "http://localhost:8085/api/customers" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Smith",
    "email": "jane.smith@example.com",
    "phone": "5555555555",
    "address": "789 Pine Street"
  }'
```

### Create Current Account

```bash
curl -X POST "http://localhost:8085/api/accounts/current" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "initialBalance=2000&overDraft=1000&customerId=1"
```

### Deposit Money

```bash
curl -X POST "http://localhost:8085/api/accounts/{accountId}/credit" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "amount=500&description=Bonus payment"
```

### Get Account Balance

```bash
curl -X GET "http://localhost:8085/api/accounts/{accountId}"
```

## 🔍 Error Testing

### Test Customer Not Found

```bash
curl -X GET "http://localhost:8085/api/customers/999999"
# Expected: 404 Not Found
```

### Test Account Not Found

```bash
curl -X GET "http://localhost:8085/api/accounts/INVALID_ACCOUNT"
# Expected: 404 Not Found
```

### Test Invalid Customer Data

```bash
curl -X POST "http://localhost:8085/api/customers" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "email": "invalid-email",
    "phone": "",
    "address": ""
  }'
# Expected: 400 Bad Request with validation errors
```

## 📊 Sample Test Data

The application automatically creates sample data on startup:

- 3 sample customers
- Multiple accounts (Current and Saving)
- Sample transactions

You can use this data for immediate testing without creating new records.

## 🎯 Testing Checklist

- [ ] Create customer successfully
- [ ] Create current account with overdraft
- [ ] Create saving account with interest rate
- [ ] Perform credit operation
- [ ] Perform debit operation
- [ ] Transfer money between accounts
- [ ] Check account balance after operations
- [ ] View account transaction history
- [ ] Apply interest to saving account
- [ ] Search customers by keyword
- [ ] Update customer information
- [ ] Test error scenarios (404, 400)
- [ ] Check dashboard statistics
- [ ] Verify Swagger documentation

## 🔧 Troubleshooting

### Application Won't Start

1. Check if port 8085 is available
2. Verify Java 21 is installed
3. Run `mvn clean compile` first

### Tests Failing

1. Run unit tests: `mvn test -Dtest="CustomerControllerTest,BankAccountControllerTest"`
2. Check application logs for errors
3. Verify database connectivity

### API Not Responding

1. Check application status: `GET /api/dashboard/health`
2. Verify correct port and URL
3. Check firewall settings

## 📚 Additional Resources

- **Swagger UI**: `http://localhost:8085/swagger-ui.html`
- **API Docs**: `http://localhost:8085/v3/api-docs`
- **H2 Console**: `http://localhost:8085/h2-console` (if enabled)
- **Application Logs**: Check console output for detailed logging
