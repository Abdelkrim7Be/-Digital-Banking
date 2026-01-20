FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/config-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]

