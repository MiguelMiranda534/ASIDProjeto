FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml /app/pom.xml
COPY Servico-Query /app/Servico-Query
WORKDIR /app/Servico-Query
RUN mvn package -DskipTests
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/Servico-Query/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]