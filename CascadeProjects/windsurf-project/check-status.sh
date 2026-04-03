#!/bin/bash

# Sports Betting System Status Monitor
echo "========================================"
echo "  Sports Betting System Status"
echo "========================================"
echo

# Function to check service status
check_service_status() {
    local url=$1
    local service_name=$2
    
    if response=$(curl -s "$url" 2>/dev/null); then
        echo "  Status: UP"
        echo "  Response: $response"
        return 0
    else
        echo "  Status: DOWN"
        echo "  Error: $service_name not reachable"
        return 1
    fi
}

# Check Event API Service
echo "Event API Service (Port 8080):"
if check_service_status "http://localhost:8080/api/events/health" "Event API"; then
    echo "  URL: http://localhost:8080"
    echo "  Outbox Endpoint: /api/events/outcome"
fi
echo

# Check Settlement Service
echo "Settlement Service (Port 8081):"
if check_service_status "http://localhost:8081/actuator/health" "Settlement"; then
    echo "  URL: http://localhost:8081"
    
    # Check metrics if available
    if curl -s "http://localhost:8081/actuator/metrics" >/dev/null 2>&1; then
        echo "  Metrics: Available"
    else
        echo "  Metrics: Not available"
    fi
fi
echo

# Check H2 Consoles
echo "Database Consoles:"
echo "  Event API H2: http://localhost:8080/h2-console"
echo "  Settlement H2: http://localhost:8081/h2-console"
echo

# Sample test commands
echo "Quick Test Commands:"
echo "1. Test Event API:"
echo "   curl -X POST http://localhost:8080/api/events/outcome \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"eventId\":\"TEST\",\"eventName\":\"QuickTest\",\"winnerId\":\"T1\"}'"
echo
echo "2. Check Health:"
echo "   curl http://localhost:8080/api/events/health"
echo "   curl http://localhost:8081/actuator/health"
echo

echo "========================================"
echo "  Expected System Behavior:"
echo "========================================"
echo "1. Event API saves to OUTBOX_EVENTS table"
echo "2. Outbox publisher sends to Kafka topic"
echo "3. Settlement service consumes from Kafka"
echo "4. Settlement matches bets and creates settlements"
echo "5. Settlement publisher sends to RocketMQ"
echo

echo "Sample Bets:"
echo "BET001: USER001 - E1 - T1 (Should WIN if T1 wins E1)"
echo "BET002: USER002 - E1 - T2 (Should LOSE if T1 wins E1)"
echo "BET003: USER003 - E1 - T1 (Should WIN if T1 wins E1)"
echo "BET004: USER004 - E2 - T3 (Should LOSE if T2 wins E2)"
echo "BET005: USER005 - E2 - T4 (Should LOSE if T2 wins E2)"
echo

echo "Process Status:"
if pgrep -f "event-api-service" >/dev/null; then
    echo "  Event API Service: RUNNING"
else
    echo "  Event API Service: STOPPED"
fi

if pgrep -f "settlement-service" >/dev/null; then
    echo "  Settlement Service: RUNNING"
else
    echo "  Settlement Service: STOPPED"
fi

echo
echo "Log Files:"
if [ -f "event-api.log" ]; then
    echo "  Event API: event-api.log ($(wc -l < event-api.log) lines)"
else
    echo "  Event API: event-api.log (not found)"
fi

if [ -f "settlement.log" ]; then
    echo "  Settlement: settlement.log ($(wc -l < settlement.log) lines)"
else
    echo "  Settlement: settlement.log (not found)"
fi

echo
echo "Refresh with: ./check-status.sh"
