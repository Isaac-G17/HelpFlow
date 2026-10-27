# ---------- Etapa 1: build (Maven + JDK) ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Primero solo el pom: la capa de dependencias se cachea mientras el pom no cambie.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# ---------- Etapa 2: runtime (JRE liviano, usuario sin privilegios) ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=10 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
