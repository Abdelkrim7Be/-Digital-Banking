#!/bin/bash

# API Testing Script for Digital Banking Microservices
# This script tests the main API endpoints

set -e

GATEWAY_URL="http://localhost:8080"
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo "🧪 Testing Digital Banking API..."
echo ""

# Test 1: Register User
echo -e "${YELLOW}1. Testing User Registration...${NC}"
REGISTER_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "name": "Test User"
  }')

if echo "$REGISTER_RESPONSE" | grep -q "id\|token"; then
    echo -e "${GREEN}✅ Registration successful${NC}"
else
    echo -e "${RED}❌ Registration failed${NC}"
    echo "$REGISTER_RESPONSE"
fi
echo ""

# Test 2: Login
echo -e "${YELLOW}2. Testing User Login...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }')

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
    echo -e "${GREEN}✅ Login successful${NC}"
    echo "Token: ${TOKEN:0:50}..."
else
    echo -e "${RED}❌ Login failed${NC}"
    echo "$LOGIN_RESPONSE"
    exit 1
fi
echo ""

# Test 3: Get Customers (Protected)
if [ -n "$TOKEN" ]; then
    echo -e "${YELLOW}3. Testing Protected Endpoint (Get Customers)...${NC}"
    CUSTOMERS_RESPONSE=$(curl -s -X GET "$GATEWAY_URL/api/customers" \
      -H "Authorization: Bearer $TOKEN")
    
    if echo "$CUSTOMERS_RESPONSE" | grep -q "\[\|\{"; then
        echo -e "${GREEN}✅ Protected endpoint accessible${NC}"
    else
        echo -e "${RED}❌ Protected endpoint failed${NC}"
        echo "$CUSTOMERS_RESPONSE"
    fi
    echo ""
fi

# Test 4: Health Check
echo -e "${YELLOW}4. Testing Gateway Health...${NC}"
HEALTH=$(curl -s "$GATEWAY_URL/actuator/health")
if echo "$HEALTH" | grep -q "UP\|status"; then
    echo -e "${GREEN}✅ Gateway is healthy${NC}"
else
    echo -e "${RED}❌ Gateway health check failed${NC}"
fi
echo ""

echo -e "${GREEN}🎉 API Testing Complete!${NC}"

