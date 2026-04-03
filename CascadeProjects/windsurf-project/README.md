# 🎯 Sports Betting Settlement System

A production-grade, multi-module Spring Boot application implementing a sports betting settlement system using event-driven architecture with Outbox Pattern.

## 🏗️ Architecture Overview

The system consists of three main components:

1. **event-api-service** (Port 8080) - REST API for accepting event outcomes
2. **settlement-service** (Port 8081) - Processes events and settles bets
3. **common-lib** - Shared DTOs and constants

### Event Flow

```
Event Outcome → event-api-service → Outbox Table → Kafka → settlement-service → Settlement Outbox → RocketMQ
```

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Maven 3.8+
- curl (for testing)

### Option 1: Full Auto Start & Traffic Generation (Recommended)

```bash
# Make executable and run
chmod +x start-and-test.sh
./start-and-test.sh
```

This single command will:
- Build the project
- Start both services
- Generate continuous test traffic
- Monitor system health
- Provide real-time feedback

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

## 📊 Usage Example

### Send Event Outcome

```bash
curl -X POST http://localhost:8080/api/events/outcome \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "E1",
    "eventName": "Match",
    "winnerId": "T1"
  }'
```

Expected Response:
```http
HTTP/1.1 202 Accepted
```

### Monitor Processing

1. **Check Outbox Tables**
   - Event API: http://localhost:8080/h2-console
   - Settlement Service: http://localhost:8081/h2-console

2. **Monitor Kafka Topics**
   - Use any Kafka UI tool to monitor `event-outcomes` topic

3. **Monitor RocketMQ**
   - Console: http://localhost:8082
   - Check `bet-settlements` topic

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Tests with Coverage

```bash
mvn verify
```

Coverage reports will be generated in `target/site/jacoco/index.html`

### Test Categories

1. **Unit Tests** - Service layer with Mockito
2. **Integration Tests** - Spring Boot tests with MockMvc
3. **End-to-End Tests** - Testcontainers with Kafka/RocketMQ

### Run Specific Test Categories

```bash
# Unit tests only
mvn test -Dtest="**/*Test"

# Integration tests only
mvn test -Dtest="**/*IntegrationTest"

# Testcontainers tests
mvn test -Dtest="**/*EndToEndTest"
```

## 🐳 Docker Deployment

For production deployment with external Kafka/RocketMQ:

```bash
# Start infrastructure services
docker-compose up -d

# Then run services with external configuration
export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export ROCKETMQ_NAME_SERVER=localhost:9876

./start-services.sh
```

### Infrastructure Services
- Zookeeper (Port 2181)
- Kafka (Port 9092)
- RocketMQ NameServer (Port 9876)
- RocketMQ Broker (Ports 10909, 10911, 10912)
- RocketMQ Console (Port 8082)

## 📁 Project Structure

```
sports-betting-system/
├── docker-compose.yml                 # Infrastructure services
├── start-and-test.sh                  # Full automation with traffic generation
├── start-services.sh                  # Simple service startup
├── check-status.sh                    # System status monitor
├── RUNNING.md                          # Detailed running instructions
├── pom.xml                           # Parent Maven POM
├── common-lib/                       # Shared DTOs and utilities
│   ├── src/main/java/com/sportsbetting/common/
│   │   ├── dto/                      # EventOutcome, Bet, SettlementEvent
│   │   └── constants/                # Topics, constants
│   └── src/test/java/                # Unit tests
├── event-api-service/                 # REST API service
│   ├── src/main/java/com/sportsbetting/eventapi/
│   │   ├── controller/               # EventController
│   │   ├── service/                   # EventService
│   │   ├── repository/               # OutboxEventRepository
│   │   ├── domain/                   # OutboxEvent, OutboxStatus
│   │   ├── messaging/                # OutboxPublisher
│   │   └── config/                   # KafkaConfig
│   ├── src/test/java/                # Tests
│   └── Dockerfile
├── settlement-service/                # Settlement processing service
│   ├── src/main/java/com/sportsbetting/settlement/
│   │   ├── service/                   # SettlementService
│   │   ├── repository/               # BetRepository, SettlementOutboxRepository
│   │   ├── domain/                   # SettlementOutbox, OutboxStatus, BetEntity
│   │   ├── messaging/                # KafkaEventConsumer, RocketMQPublisher
│   │   └── config/                   # KafkaConfig, DataInitializer
│   ├── src/test/java/                # Tests
│   └── Dockerfile
└── src/test/java/                    # System integration tests
```

## 🔧 Configuration

### Application Properties

#### Event API Service (application.yml)
```yaml
server:
  port: 8080
spring:
  kafka:
    bootstrap-servers: localhost:9092
app:
  topics:
    event-outcomes: event-outcomes
  outbox:
    poll-interval: 5000
```

#### Settlement Service (application.yml)
```yaml
server:
  port: 8081
spring:
  kafka:
    bootstrap-servers: localhost:9092
rocketmq:
  name-server: localhost:9876
app:
  topics:
    event-outcomes: event-outcomes
    bet-settlements: bet-settlements
  settlement:
    payout-multiplier: 2.0
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka bootstrap servers |
| `ROCKETMQ_NAME_SERVER` | `localhost:9876` | RocketMQ name server |
| `DB_URL` | `jdbc:h2:mem:testdb` | Database URL |
| `DB_USERNAME` | `sa` | Database username |
| `DB_PASSWORD` | `password` | Database password |

## 📈 Monitoring & Observability

### Health Endpoints

- Event API Service: `GET http://localhost:8080/api/events/health`
- Settlement Service: `GET http://localhost:8081/actuator/health`

### Metrics

Both services expose Spring Boot Actuator metrics:
- `GET http://localhost:8080/actuator/metrics`
- `GET http://localhost:8081/actuator/metrics`

### Logging

Configure log levels in `application.yml`:

```yaml
logging:
  level:
    com.sportsbetting: DEBUG
    org.springframework.kafka: INFO
    org.apache.rocketmq: INFO
```

## 🔍 Troubleshooting

### Common Issues

1. **Kafka Connection Failed**
   ```bash
   # Check Kafka status
   docker-compose logs kafka
   # Verify Kafka is running on port 9092
   ```

2. **RocketMQ Connection Failed**
   ```bash
   # Check RocketMQ status
   docker-compose logs rocketmq-nameserver
   docker-compose logs rocketmq-broker
   ```

3. **Outbox Events Not Processing**
   - Check database tables via H2 console
   - Verify Kafka topic exists
   - Check application logs for errors

4. **Test Failures**
   ```bash
   # Clean build and test
   mvn clean test
   # Check if Docker containers are running
   docker-compose ps
   ```

### Debug Mode

Enable debug logging:

```yaml
logging:
  level:
    com.sportsbetting: DEBUG
    org.springframework.kafka: DEBUG
    org.apache.rocketmq: DEBUG
    org.hibernate.SQL: DEBUG
```

## 🧰 Development

### Code Formatting

Project uses Google Java Format:

```bash
# Format all code
mvn fmt:format

# Check formatting
mvn fmt:check
```

### Database Schema

The system uses H2 in-memory database with auto-DDL creation. Schema includes:

**Event API Service:**
- `outbox_events` table for Outbox Pattern

**Settlement Service:**
- `bets` table (sample data loaded on startup)
- `settlement_outbox` table for Outbox Pattern

### Adding New Services

1. Create new module in parent POM
2. Add dependencies in parent POM dependencyManagement
3. Follow same package structure as existing services
4. Add Dockerfile for containerization

## 📋 API Documentation

### Event API

#### POST /api/events/outcome

Publishes an event outcome to the system.

**Request Body:**
```json
{
  "eventId": "E1",
  "eventName": "Match",
  "winnerId": "T1"
}
```

**Response:**
- `202 Accepted` - Event accepted for processing
- `400 Bad Request` - Invalid event data
- `500 Internal Server Error` - Processing error

#### GET /api/events/health

Health check endpoint.

**Response:**
```json
"Event API Service is healthy"
```

## 🎯 Business Logic

### Settlement Rules

1. **Winning Condition**: `bet.eventWinnerId == event.winnerId`
2. **Payout Calculation**: `betAmount * payoutMultiplier` (default: 2.0)
3. **Losing Bets**: Payout amount = 0

### Idempotency

- Event API: Checks for duplicate events by eventId
- Settlement Service: Checks for duplicate settlements by settlementId
- Outbox Pattern: Prevents duplicate processing

## 📄 License

This project is licensed under the MIT License.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Run `mvn verify` to ensure all tests pass
6. Submit a pull request

## 📞 Support

For issues and questions:
- Check troubleshooting section
- Review application logs
- Verify infrastructure services are running
