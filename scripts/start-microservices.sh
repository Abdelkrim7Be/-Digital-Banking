#!/bin/bash

# Digital Banking Microservices Startup Script
# This script helps start all microservices in the correct order

set -e

echo "🚀 Starting Digital Banking Microservices..."
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Docker is running${NC}"
echo ""

# Check if docker-compose file exists
if [ ! -f "docker-compose-microservices.yml" ]; then
    echo -e "${RED}❌ docker-compose-microservices.yml not found${NC}"
    exit 1
fi

echo -e "${YELLOW}📦 Building and starting services...${NC}"
echo ""

# Start services
docker-compose -f docker-compose-microservices.yml up --build -d

echo ""
echo -e "${GREEN}✅ All services are starting...${NC}"
echo ""
echo "📋 Service URLs:"
echo "   - Discovery Service: http://localhost:8761"
echo "   - Gateway Service:   http://localhost:8080"
echo "   - Zipkin:            http://localhost:9411"
echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check service health
echo ""
echo "🔍 Checking service health..."

services=("discovery-service:8761" "gateway-service:8080")
for service in "${services[@]}"; do
    name=$(echo $service | cut -d: -f1)
    port=$(echo $service | cut -d: -f2)
    if curl -s http://localhost:$port > /dev/null 2>&1; then
        echo -e "${GREEN}✅ $name is up${NC}"
    else
        echo -e "${YELLOW}⏳ $name is starting...${NC}"
    fi
done

echo ""
echo -e "${GREEN}🎉 Services are starting!${NC}"
echo ""
echo "📝 Useful commands:"
echo "   View logs:    docker-compose -f docker-compose-microservices.yml logs -f"
echo "   Stop all:     docker-compose -f docker-compose-microservices.yml down"
echo "   View status:  docker-compose -f docker-compose-microservices.yml ps"
echo ""

