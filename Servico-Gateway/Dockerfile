FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml /app/pom.xml
COPY Servico-Gateway /app/Servico-Gateway
WORKDIR /app/Servico-Gateway
RUN mvn package -DskipTests
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/Servico-Gateway/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]