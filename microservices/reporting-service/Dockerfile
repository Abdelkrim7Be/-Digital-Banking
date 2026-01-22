# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy parent POM and reporting-service POM
COPY ../pom.xml /app/pom.xml
COPY pom.xml /app/reporting-service/pom.xml
COPY src /app/reporting-service/src

# Build the application
WORKDIR /app/reporting-service
RUN apt-get update && apt-get install -y maven || \
    (curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz | tar xz -C /opt && \
     export PATH="/opt/apache-maven-3.9.6/bin:$PATH" && \
     mvn clean package -DskipTests)

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/reporting-service/target/reporting-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]

