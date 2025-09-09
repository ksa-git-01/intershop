FROM maven:3.9.9-amazoncorretto-21 AS build

WORKDIR /app

# Копируем все pom
COPY pom.xml .
COPY store/pom.xml ./store/
COPY payment/pom.xml ./payment/

# Скачиваем зависимости для всего проекта
RUN mvn dependency:go-offline -B

# Копируем openapi схему для генерации web клиента в store
COPY payment/src ./payment/src
COPY store/src ./store/src

# Собираем только store модуль
WORKDIR /app/store
RUN mvn clean package -DskipTests

FROM amazoncorretto:21
WORKDIR /app

COPY --from=build /app/store/target/*.jar app.jar
RUN mkdir -p /app/uploads

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]