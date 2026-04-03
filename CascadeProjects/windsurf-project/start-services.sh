#!/bin/bash

# Simple service startup script
echo "Starting Sports Betting Settlement Services..."

# Build project
echo "Building project..."
mvn clean compile -DskipTests -q

# Start Event API Service
echo "Starting Event API Service..."
cd event-api-service
mvn spring-boot:run &
cd ..
echo "Event API Service started on http://localhost:8080"

# Start Settlement Service
echo "Starting Settlement Service..."
cd settlement-service  
mvn spring-boot:run &
cd ..
echo "Settlement Service started on http://localhost:8081"

echo
echo "Services starting up..."
echo "Check status with: ./check-status.sh"
echo "Generate traffic with: ./start-and-test.sh"
echo
echo "Press Ctrl+C to stop services"
