# How to run Digital Banking (same as the main dev setup)

Your friend can run the app the same way you do with one script. Here’s what they need and what to do.

## Prerequisites (install once)

- **Docker** (and Docker Compose) – used to start Kafka.
- **Java 17+** – for the Spring Boot microservices.
- **Maven 3.6+** – to build and run the backend.
- **Node.js 18+** and **npm** – for the Angular frontend.

## Run the app

1. Clone the repo and go to the project root:

   ```bash
   cd /path/to/Digital-Banking
   ```

2. Make the script executable (only needed once):

   ```bash
   chmod +x run-with-kafka.sh
   ```

3. Start everything (Kafka, discovery, config, gateway, microservices, frontend):
   ```bash
   ./run-with-kafka.sh
   ```

The script will:

- Start Kafka (and Zookeeper) in Docker.
- Build the microservices with Maven and the frontend with npm.
- Start discovery, config, gateway, customer-service, account-service, transaction-service, reporting-service, then the frontend.

Wait until you see the lines with the URLs (about 1–2 minutes). Then open:

- **Frontend (main app):** http://localhost:4200
- **Gateway (API):** http://localhost:8080
- **Eureka (service list):** http://localhost:8761

## Demo logins (seeded automatically)

- **Admin:** `admin` / `password`
- **Customer:** `marie.dupont` / `password` (or any of the 50 demo customers, same password)

## Stop the app

Press **Ctrl+C** in the terminal where the script is running. It will stop the frontend, microservices, and bring down the Kafka stack.

## If something goes wrong

- **“Port 8761 already in use”** – Another run or another app is using that port. Stop it or kill the process using the port, then run the script again.
- **Discovery or gateway fails** – Check `logs/discovery-service.log` and `logs/gateway-service.log` in the project root.
- **Frontend won’t load** – Ensure you’re using http://localhost:4200 and that nothing else is using port 4200.

No need to run the `seed-data.sql` file when using this setup: the app seeds demo users, accounts, and transactions on startup.
