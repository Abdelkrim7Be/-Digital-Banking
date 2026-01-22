# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy parent POM and customer-service POM
COPY ../pom.xml /app/pom.xml
COPY pom.xml /app/customer-service/pom.xml
COPY src /app/customer-service/src

# Build the application
WORKDIR /app/customer-service
RUN apt-get update && apt-get install -y maven || \
    (curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz | tar xz -C /opt && \
     export PATH="/opt/apache-maven-3.9.6/bin:$PATH" && \
     mvn clean package -DskipTests)

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/customer-service/target/customer-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
