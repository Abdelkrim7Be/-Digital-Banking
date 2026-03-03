# Digital Banking (MVP)

Banking app with admin and customer portals. Backend: Spring Boot microservices with Maven and Eureka; frontend: Angular.

**Version 2** adds an optional event-driven mode with Apache Kafka: services publish domain events (customer created, account created, balance updates, transactions); reporting-service consumes them for audit when Kafka is enabled.

## Run (no Kafka)

```bash
./run-all.sh
```

Starts: Eureka (8761), Config, Gateway (8080), customer/account/transaction/reporting services, Angular (4200). Logs in `./logs/`.

## Run (event-driven with Kafka)

Requires Docker. Starts Kafka then all services with the `kafka` profile.

```bash
./run-with-kafka.sh
```

Uses `docker-compose-kafka.yml` (Zookeeper + Kafka on 9092). Topics: `customer-events`, `account-events`, `account-balance-updates`, `transaction-events`. Reporting-service consumes all when Kafka is enabled. Project name `digital-banking-kafka` avoids container name conflicts.

## Stack

- **Gateway** (8080): JWT auth, routes to services
- **Discovery**: Eureka (8761)
- **customer-service**: auth, users, customer CRUD, portal; publishes `customer-events`
- **account-service**: bank accounts; publishes `account-events`, `account-balance-updates`
- **transaction-service**: credit, debit, transfer; publishes `transaction-events`
- **reporting-service**: admin dashboard (Feign); consumes all event topics when Kafka is enabled
- **Frontend** (4200): Angular, admin + customer UIs (SPA)

Demo logins: `admin` / `marie.dupont` / `jean.martin` with password `password`.
