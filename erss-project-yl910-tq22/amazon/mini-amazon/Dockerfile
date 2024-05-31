
FROM maven:3.8.1-openjdk-11-slim AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src /app/src

RUN ls /app/src

RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

COPY src/main/resources/products.json /app/data/products.json

EXPOSE 8081 

ENTRYPOINT ["java", "-jar", "app.jar"]