FROM openjdk:21-jdk-slim

LABEL maintainer="sports-betting-team"
LABEL description="Event API Service for Sports Betting System"

WORKDIR /app

COPY target/event-api-service-*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/events/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
