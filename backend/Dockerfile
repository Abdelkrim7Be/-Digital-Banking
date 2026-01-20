# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml pom.xml
COPY src src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=build /app/target/digital-banking-backend-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8085
ENV SPRING_PROFILES_ACTIVE=dev
ENTRYPOINT ["java", "-jar", "app.jar"]

