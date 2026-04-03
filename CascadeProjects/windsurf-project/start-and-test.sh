#!/bin/bash

# Sports Betting Settlement System - Auto Start & Test Script (Git Bash Compatible)
set -e

echo "========================================"
echo "  Sports Betting Settlement System"
echo "  Auto Start & Traffic Generator"
echo "========================================"
echo

# Function to check if service is ready
check_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    echo "Checking $service_name..."
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" >/dev/null 2>&1; then
            echo "✓ $service_name is ready!"
            return 0
        fi
        echo "  Attempt $attempt/$max_attempts - waiting for $service_name..."
        sleep 2
        ((attempt++))
    done
    
    echo "✗ $service_name failed to start within timeout"
    return 1
}

# Function to send event
send_event() {
    local event_id=$1
    local event_name=$2
    local winner_id=$3
    
    echo "[$(date '+%H:%M:%S')] Sending event: $event_id - $event_name - $winner_id"
    
    # Use curl for HTTP POST
    local response=$(curl -s -w "%{http_code}" -o /dev/null -X POST \
        "http://localhost:8080/api/events/outcome" \
        -H "Content-Type: application/json" \
        -d "{\"eventId\":\"$event_id\",\"eventName\":\"$event_name\",\"winnerId\":\"$winner_id\"}" \
        --connect-timeout 10 --max-time 10)
    
    if [ "$response" = "202" ]; then
        echo "  ✓ Event sent successfully"
    else
        echo "  ✗ Failed to send event (HTTP: $response)"
    fi
}

# Step 1: Build project
echo "[1/4] Building project..."
mvn clean install -DskipTests -q
echo "Build completed successfully!"
echo

# Step 2: Start Event API Service
echo "[2/4] Starting Event API Service (Port 8080)..."
cd event-api-service
mvn spring-boot:run -q > ../event-api.log 2>&1 &
EVENT_API_PID=$!
cd ..
echo "Event API Service started with PID: $EVENT_API_PID"

# Step 3: Start Settlement Service
echo "[3/4] Starting Settlement Service (Port 8081)..."
cd settlement-service
mvn spring-boot:run -q > ../settlement.log 2>&1 &
SETTLEMENT_PID=$!
cd ..
echo "Settlement Service started with PID: $SETTLEMENT_PID"
echo

# Step 4: Wait for services to be ready
echo "[4/4] Waiting for services to be ready..."
check_service "http://localhost:8080/api/events/health" "Event API Service"
check_service "http://localhost:8081/actuator/health" "Settlement Service"

echo
echo "========================================"
echo "  SERVICES ARE RUNNING!"
echo "========================================"
echo
echo "Event API Service:     http://localhost:8080"
echo "Settlement Service:    http://localhost:8081"
echo "H2 Console (Event):   http://localhost:8080/h2-console"
echo "H2 Console (Settle):  http://localhost:8081/h2-console"
echo "Event API Logs:        event-api.log"
echo "Settlement Logs:       settlement.log"
echo

# Function to cleanup on exit
cleanup() {
    echo
    echo "Shutting down services..."
    if [ ! -z "$EVENT_API_PID" ]; then
        kill $EVENT_API_PID 2>/dev/null || true
        echo "Event API Service stopped"
    fi
    if [ ! -z "$SETTLEMENT_PID" ]; then
        kill $SETTLEMENT_PID 2>/dev/null || true
        echo "Settlement Service stopped"
    fi
    exit 0
}

# Set trap for cleanup
trap cleanup SIGINT SIGTERM

echo "========================================"
echo "  GENERATING TEST TRAFFIC"
echo "========================================"
echo

# Traffic generation loop
counter=0
while true; do
    ((counter++))
    echo "=== Traffic Cycle $counter ==="
    
    # Send different events
    send_event "E1" "Match" "T1"
    sleep 2
    
    send_event "E2" "Final" "T2"
    sleep 2
    
    send_event "E3" "SemiFinal" "T1"
    sleep 2
    
    send_event "E4" "QuarterFinal" "T3"
    sleep 2
    
    send_event "E5" "Test" "T2"
    sleep 2
    
    # Send duplicate to test idempotency
    echo "[$(date '+%H:%M:%S')] Sending duplicate event to test idempotency: E1"
    send_event "E1" "Match" "T1"
    sleep 3
    
    echo
    echo "========================================"
    echo "  CHECKING SYSTEM STATUS"
    echo "========================================"
    
    # Check Event API health
    echo "Event API Health:"
    if curl -s "http://localhost:8080/api/events/health" >/dev/null 2>&1; then
        echo "  Service is UP"
    else
        echo "  Service is DOWN"
    fi
    
    # Check Settlement Service health
    echo "Settlement Health:"
    if curl -s "http://localhost:8081/actuator/health" >/dev/null 2>&1; then
        echo "  Service is UP"
    else
        echo "  Service is DOWN"
    fi
    
    echo
    echo "========================================"
    echo "  SAMPLE BETS IN SYSTEM:"
    echo "========================================"
    echo "BET001: USER001 - E1 - T1 (WINNER if T1 wins)"
    echo "BET002: USER002 - E1 - T2 (LOSER if T1 wins)"
    echo "BET003: USER003 - E1 - T1 (WINNER if T1 wins)"
    echo "BET004: USER004 - E2 - T3 (LOSER if T2 wins)"
    echo "BET005: USER005 - E2 - T4 (LOSER if T2 wins)"
    echo
    
    echo "========================================"
    echo "  MONITORING INSTRUCTIONS"
    echo "========================================"
    echo "1. Check Outbox Tables:"
    echo "   - Event API: http://localhost:8080/h2-console"
    echo "   - Settlement: http://localhost:8081/h2-console"
    echo
    echo "2. View Logs:"
    echo "   - tail -f event-api.log"
    echo "   - tail -f settlement.log"
    echo
    echo "3. Test Custom Events:"
    echo "   curl -X POST http://localhost:8080/api/events/outcome \\"
    echo "     -H 'Content-Type: application/json' \\"
    echo "     -d '{\"eventId\":\"CUSTOM\",\"eventName\":\"Test\",\"winnerId\":\"T1\"}'"
    echo
    
    echo "Next cycle in 15 seconds... (Press Ctrl+C to stop)"
    sleep 15
done
