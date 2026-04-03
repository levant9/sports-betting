FROM openjdk:21-jdk-slim

LABEL maintainer="sports-betting-team"
LABEL description="Settlement Service for Sports Betting System"

WORKDIR /app

COPY target/settlement-service-*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
