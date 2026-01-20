#!/bin/bash

docker compose up --build -d

echo ""
echo "=========================================="
echo "Applications are running at:"
echo "=========================================="
echo "Frontend: http://localhost:4200"
echo "Backend API: http://localhost:8085"
echo "=========================================="
echo ""
