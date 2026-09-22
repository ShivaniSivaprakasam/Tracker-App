# ==================== Stage 1: Build ====================
# Uses a full Maven+JDK image only to compile the app — this stage's
# contents (Maven, build cache, source files) are discarded and never
# ship in the final image.
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first and download dependencies separately from copying
# source code — Docker caches this layer, so re-running the build after
# only changing Java files skips re-downloading every dependency.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ==================== Stage 2: Run ====================
# A minimal JRE (not JDK) image — smaller, and doesn't need compiler
# tools since we're only running an already-built .jar.
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copies only the final built jar from the build stage — none of the
# source code, Maven cache, or build tooling make it into this image.
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Runs with the "docker" Spring profile active, so it picks up
# application-docker.properties instead of the default local config.
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]