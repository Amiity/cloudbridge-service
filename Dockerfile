FROM openjdk:17

WORKDIR /app

COPY *.jar .

CMD ["java", "-jar", "CloudBridge-0.0.1-SNAPSHOT.jar"]