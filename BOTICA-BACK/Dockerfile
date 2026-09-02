# syntax=docker/dockerfile:1.7

FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

# Descarga las dependencias en una capa reutilizable. El código fuente cambia
# con mucha más frecuencia que el pom.xml.
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app

COPY --from=build --chown=spring:spring /workspace/target/*.jar /app/app.jar

USER spring:spring
EXPOSE 9091

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=5 \
    CMD wget -q --spider http://127.0.0.1:9091/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
