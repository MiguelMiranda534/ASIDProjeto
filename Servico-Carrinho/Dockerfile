FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml /app/pom.xml
COPY Servico-Carrinho /app/Servico-Carrinho
WORKDIR /app/Servico-Carrinho
RUN mvn package -DskipTests
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/Servico-Carrinho/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]