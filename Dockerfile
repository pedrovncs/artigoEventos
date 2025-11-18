FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY target/artigoEventos-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /app/uploads
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]