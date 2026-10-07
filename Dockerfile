# Multi-stage build of the Pozzo RESTful services.
#
# Step 1 compiles the application with the official Maven image, which already bundles Maven,
# so the build does not depend on the Maven Wrapper downloading Maven on every deploy.
# Step 2 runs the jar on a slim Java 21 runtime.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn package -DskipTests -B -q

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
RUN useradd --system --no-create-home pozzo
COPY --from=build /app/target/*.jar app.jar
USER pozzo

# The hosting platform assigns the port through PORT; 8080 is the local default.
EXPOSE 8080

# Keep the heap within the memory limit of small instances (512 MB on Render's free plan).
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]

# Environment variables required at runtime (define them in the hosting platform):
# - DATABASE_URL       JDBC URL of PostgreSQL (Supabase session pooler)
# - DATABASE_USERNAME  database user
# - DATABASE_PASSWORD  database password
# - JWT_SECRET         signing key of the tokens, at least 32 characters
# - PORT               set automatically by Render
