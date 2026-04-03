# 🚀 Running the Sports Betting System (Linux/macOS)

## Quick Start

### Option 1: Full Auto Start & Traffic Generation
```bash
# Make executable and run
chmod +x start-and-test.sh
./start-and-test.sh
```

### Option 2: Start Services Only
```bash
chmod +x start-services.sh
./start-services.sh
```

### Option 3: Check System Status
```bash
chmod +x check-status.sh
./check-status.sh
```

## Script Descriptions

### `start-and-test.sh` - Full Automation
- ✅ Builds the project
- ✅ Starts both services in background
- ✅ Waits for services to be ready
- ✅ Generates continuous test traffic
- ✅ Monitors system health
- ✅ Provides real-time feedback
- ✅ Graceful shutdown on Ctrl+C

### `start-services.sh` - Simple Start
- ✅ Builds the project
- ✅ Starts both services
- ✅ Minimal output

### `check-status.sh` - Status Monitor
- ✅ Checks service health
- ✅ Shows process status
- ✅ Displays log file info
- ✅ Provides test commands

## What the Auto Script Does

### Traffic Generation Pattern
Every 15 seconds, the script sends:
1. **E1 - Match → T1** (3 winning bets, 1 losing bet)
2. **E2 - Final → T2** (0 winning bets, 2 losing bets)
3. **E3 - SemiFinal → T1** (no matching bets)
4. **E4 - QuarterFinal → T3** (no matching bets)
5. **E5 - Test → T2** (no matching bets)
6. **Duplicate E1** (tests idempotency)

### Sample Bets in System
- **BET001**: USER001 - E1 - T1 (WINS if T1 wins E1)
- **BET002**: USER002 - E1 - T2 (LOSES if T1 wins E1)
- **BET003**: USER003 - E1 - T1 (WINS if T1 wins E1)
- **BET004**: USER004 - E2 - T3 (LOSES if T2 wins E2)
- **BET005**: USER005 - E2 - T4 (LOSES if T2 wins E2)

## Monitoring

### Web Interfaces
- **Event API**: http://localhost:8080
- **Settlement**: http://localhost:8081
- **H2 Console (Event)**: http://localhost:8080/h2-console
- **H2 Console (Settlement)**: http://localhost:8081/h2-console

### Log Files
- **Event API**: `event-api.log`
- **Settlement**: `settlement.log`

### View Logs Live
```bash
# Event API logs
tail -f event-api.log

# Settlement logs  
tail -f settlement.log

# Both logs
tail -f event-api.log settlement.log
```

## Manual Testing

### Send Custom Events
```bash
curl -X POST http://localhost:8080/api/events/outcome \
  -H "Content-Type: application/json" \
  -d '{"eventId":"CUSTOM","eventName":"ManualTest","winnerId":"T1"}'
```

### Check Health
```bash
curl http://localhost:8080/api/events/health
curl http://localhost:8081/actuator/health
```

## Database Monitoring

### Event API Database
1. Go to: http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:testdb`
3. Username: `sa`
4. Password: `password`
5. Check `OUTBOX_EVENTS` table

### Settlement Database  
1. Go to: http://localhost:8081/h2-console
2. JDBC URL: `jdbc:h2:mem:testdb`
3. Username: `sa`
4. Password: `password`
5. Check `BETS` and `SETTLEMENT_OUTBOX` tables

## Expected Behavior

### Event API Service
1. Receives HTTP POST to `/api/events/outcome`
2. Saves to `OUTBOX_EVENTS` table as `PENDING`
3. Scheduled publisher processes and sends to Kafka
4. Updates status to `PUBLISHED`

### Settlement Service
1. Consumes from Kafka topic `event-outcomes`
2. Matches bets by `eventId` and `winnerId`
3. Creates settlement events for winning/losing bets
4. Saves to `SETTLEMENT_OUTBOX` table
5. Scheduled publisher sends to RocketMQ

## Troubleshooting

### Services Not Starting
```bash
# Check Java version
java -version

# Check Maven
mvn -version

# Clean build
mvn clean compile -DskipTests
```

### Port Conflicts
```bash
# Check what's using ports
lsof -i :8080
lsof -i :8081

# Kill processes if needed
kill -9 <PID>
```

### Services Not Responding
```bash
# Wait longer for startup
./check-status.sh

# Check logs for errors
tail -f event-api.log
tail -f settlement.log
```

## Stop Services

### If Using Auto Script
Press `Ctrl+C` in the terminal running `start-and-test.sh`

### If Using Simple Start
```bash
# Find and kill processes
pkill -f "event-api-service"
pkill -f "settlement-service"
```

## Architecture Flow

```
HTTP Request → Event API → Outbox Table → Kafka → Settlement → Bet Matching → Settlement Outbox → RocketMQ
```

The system demonstrates:
- ✅ Outbox Pattern implementation
- ✅ Event-driven architecture
- ✅ Idempotency handling
- ✅ Error handling and retries
- ✅ Real-time monitoring
- ✅ Production-grade logging
