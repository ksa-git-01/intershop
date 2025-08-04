FROM maven:3.9.9-amazoncorretto-21 AS build

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем pom.xml и скачиваем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем проект
COPY src ./src
RUN mvn clean package -DskipTests

# Запуск
FROM amazoncorretto:21
WORKDIR /app

# Копируем JAR файл
COPY --from=build /app/target/*.jar app.jar

# Создаем директорию для uploads
RUN mkdir -p /app/uploads

# Открываем порт 8080
EXPOSE 8080

# Запускаем Spring Boot приложение
CMD ["java", "-jar", "app.jar"]