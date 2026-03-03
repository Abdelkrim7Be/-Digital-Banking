#!/usr/bin/env bash

set -e
cd "$(dirname "$0")"
PROJECT_ROOT="$(pwd)"

echo "=== 1/3 Building microservices ==="
cd "$PROJECT_ROOT/microservices"
mvn -q clean install -DskipTests
echo "Microservices build OK."
echo ""

echo "=== 2/3 Building frontend ==="
cd "$PROJECT_ROOT/frontend"
if [ ! -d "node_modules" ]; then
  npm install
fi
npm run build
echo "Frontend build OK."
echo ""

echo "=== 3/3 Starting services (order: discovery -> config -> gateway -> rest + frontend) ==="
trap 'echo ""; echo "Stopping all services..."; kill $(jobs -p) 2>/dev/null; exit' INT TERM EXIT

mkdir -p "$PROJECT_ROOT/logs"
LOG_DIR="$PROJECT_ROOT/logs"

echo "Starting discovery-service..."
(cd "$PROJECT_ROOT/microservices" && mvn -q -pl discovery-service spring-boot:run >> "$LOG_DIR/discovery-service.log" 2>&1) &
DISCOVERY_PID=$!
echo "  discovery-service PID: $DISCOVERY_PID"
sleep 25
if ! kill -0 $DISCOVERY_PID 2>/dev/null; then
  echo "Discovery failed to start. Check $LOG_DIR/discovery-service.log"
  exit 1
fi
echo "  Eureka up."
echo ""

echo "Starting config-service..."
(cd "$PROJECT_ROOT/microservices" && mvn -q -pl config-service spring-boot:run >> "$LOG_DIR/config-service.log" 2>&1) &
sleep 15
echo ""

echo "Starting gateway-service..."
(cd "$PROJECT_ROOT/microservices" && mvn -q -pl gateway-service spring-boot:run >> "$LOG_DIR/gateway-service.log" 2>&1) &
sleep 25
echo ""

for mod in customer-service account-service transaction-service reporting-service; do
  echo "Starting $mod..."
  (cd "$PROJECT_ROOT/microservices" && mvn -q -pl "$mod" spring-boot:run >> "$LOG_DIR/$mod.log" 2>&1) &
  sleep 8
done
echo ""

echo "Starting frontend (ng serve)..."
(cd "$PROJECT_ROOT/frontend" && npm run start >> "$LOG_DIR/frontend.log" 2>&1) &
sleep 5
echo ""

echo ""
echo "  Eureka:    http://localhost:8761"
echo "  Gateway:   http://localhost:8080"
echo "  Frontend:  http://localhost:4200"
echo ""
wait
