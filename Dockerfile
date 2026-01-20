FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy Maven wrapper and parent POM
COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml

# Copy monolith module
COPY monolith/pom.xml monolith/pom.xml
COPY monolith/src monolith/src

RUN chmod +x mvnw && ./mvnw -q -pl monolith -am clean package -DskipTests

FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

COPY --from=build /app/monolith/target/monolith-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8085

ENV SPRING_PROFILES_ACTIVE=dev

ENTRYPOINT ["java", "-jar", "app.jar"]


