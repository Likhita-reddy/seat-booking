FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml /app/pom.xml

COPY common/pom.xml /app/common/pom.xml
COPY common/src /app/common/src

COPY users-service/pom.xml /app/users-service/pom.xml
COPY booking-service/pom.xml /app/booking-service/pom.xml
COPY payment-service/pom.xml /app/payment-service/pom.xml
COPY notification-service/pom.xml /app/notification-service/pom.xml

COPY payment-service/src /app/payment-service/src

RUN mvn -f /app/pom.xml clean package \
    -pl common,payment-service \
    -am \
    -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/payment-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]