# Digital Banking (MVP)

Banking app with admin and customer portals. Backend: Spring Boot microservices with Maven and Eureka; frontend: Angular.

## Run

```bash
./run-all.sh
```

Starts: Eureka (8761), Config, Gateway (8080), customer/account/transaction/reporting services, Angular (4200). Logs go to `./logs/`.

## Stack

- **Gateway** (8080): JWT auth, routes to services
- **Discovery**: Eureka (8761)
- **customer-service**: auth, users, customer CRUD, portal (my accounts, dashboard, transactions)
- **account-service**: bank accounts
- **transaction-service**: credit, debit, transfer
- **reporting-service**: admin dashboard stats and reports
- **Frontend** (4200): Angular, admin + customer UIs

Demo logins: `admin` / `marie.dupont` / `jean.martin` with password `password`.
