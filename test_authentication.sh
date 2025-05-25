#!/bin/bash

# Script de test pour l'authentification Spring Security
echo "=== Test d'Authentification Digital Banking API ==="
echo ""

BASE_URL="http://localhost:8085"

# Test 1: Login Admin
echo "1. Test de connexion ADMIN..."
ADMIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

echo "Réponse Admin: $ADMIN_RESPONSE"
echo ""

# Extraire le token admin (simple extraction pour bash)
ADMIN_TOKEN=$(echo $ADMIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Token Admin: $ADMIN_TOKEN"
echo ""

# Test 2: Login Customer
echo "2. Test de connexion CUSTOMER..."
CUSTOMER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"abdelkrim","password":"password123"}')

echo "Réponse Customer: $CUSTOMER_RESPONSE"
echo ""

# Extraire le token customer
CUSTOMER_TOKEN=$(echo $CUSTOMER_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Token Customer: $CUSTOMER_TOKEN"
echo ""

# Test 3: Accès endpoint admin avec token admin
echo "3. Test accès endpoint admin avec token admin..."
if [ ! -z "$ADMIN_TOKEN" ]; then
    ADMIN_USERS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/admin/users" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    echo "Réponse admin/users: $ADMIN_USERS_RESPONSE"
else
    echo "Pas de token admin disponible"
fi
echo ""

# Test 4: Accès endpoint admin avec token customer (doit échouer)
echo "4. Test accès endpoint admin avec token customer (doit échouer)..."
if [ ! -z "$CUSTOMER_TOKEN" ]; then
    FORBIDDEN_RESPONSE=$(curl -s -X GET "$BASE_URL/api/admin/users" \
      -H "Authorization: Bearer $CUSTOMER_TOKEN")
    echo "Réponse (doit être 403): $FORBIDDEN_RESPONSE"
else
    echo "Pas de token customer disponible"
fi
echo ""

# Test 5: Accès endpoint customers avec token customer
echo "5. Test accès endpoint customers avec token customer..."
if [ ! -z "$CUSTOMER_TOKEN" ]; then
    CUSTOMERS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/customers" \
      -H "Authorization: Bearer $CUSTOMER_TOKEN")
    echo "Réponse customers: $CUSTOMERS_RESPONSE"
else
    echo "Pas de token customer disponible"
fi
echo ""

# Test 6: Accès sans token (doit échouer)
echo "6. Test accès sans token (doit échouer)..."
NO_TOKEN_RESPONSE=$(curl -s -X GET "$BASE_URL/api/customers")
echo "Réponse sans token: $NO_TOKEN_RESPONSE"
echo ""

echo "=== Tests terminés ==="
