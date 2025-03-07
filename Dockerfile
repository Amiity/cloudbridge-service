FROM openjdk:17

WORKDIR /app

COPY target/*.jar application.jar

CMD ["java", "-jar", "application.jar"]