FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 5555
ENTRYPOINT ["java","-jar","app.jar"]
