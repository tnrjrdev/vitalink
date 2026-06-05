# ===========================================================================
# Multi-stage build da Plataforma Medico
# Stage 1: compila o jar com Maven + JDK 11
# Stage 2: imagem final enxuta apenas com o JRE 11 (menor superficie de ataque)
# ===========================================================================

# ---------- Stage 1: build ----------
FROM maven:3.8.8-eclipse-temurin-11 AS build
WORKDIR /build

# Copia primeiro o pom para aproveitar o cache de dependencias do Docker
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copia o codigo-fonte e empacota (testes ficam para o pipeline de CI)
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:11-jre-jammy AS runtime
WORKDIR /app

# Usuario nao-root por seguranca
RUN groupadd --system medico && useradd --system --gid medico medico

COPY --from=build /build/target/medico-platform.jar app.jar
RUN chown -R medico:medico /app
USER medico

EXPOSE 8080

# Healthcheck via Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
