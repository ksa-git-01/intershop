FROM maven:3.9.9-amazoncorretto-21 AS build

WORKDIR /app

# Копируем все pom
COPY pom.xml .
COPY store/pom.xml ./store/
COPY payment/pom.xml ./payment/

# Скачиваем зависимости для всего проекта
RUN mvn dependency:go-offline -B

COPY payment/src ./payment/src

# Собираем только payment модуль
WORKDIR /app/payment
RUN mvn clean package -DskipTests

FROM amazoncorretto:21
WORKDIR /app

COPY --from=build /app/payment/target/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]